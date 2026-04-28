package com.detective.backend.service;

import com.detective.backend.domain.CaseStatus;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.Mode;
import com.detective.backend.domain.User;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final AiGenerationService aiGenerationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public DetectiveCase joinCase(Long userId, Long caseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        DetectiveCase detCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        if (detCase.getMode() != Mode.PUBLIC) {
            throw new IllegalStateException("Cannot join solo cases");
        }
        
        detCase.getParticipants().add(user);
        return caseRepository.save(detCase);
    }

    @Transactional
    public DetectiveCase leaveCase(Long userId, Long caseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        DetectiveCase detCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        if (!detCase.getParticipants().contains(user)) {
            throw new IllegalStateException("User is not a participant of this case");
        }
        if (detCase.getStatus() == CaseStatus.CLOSED || detCase.getStatus() == CaseStatus.SCORING) {
            throw new IllegalStateException("Cannot leave closed case");
        }

        detCase.getParticipants().remove(user);
        return caseRepository.save(detCase);
    }

public DetectiveCase requestCaseGeneration(User creator, Mode mode, String theme, String difficulty, Integer cluesCount, Integer deadlineDays) {
        DetectiveCase newCase = DetectiveCase.builder()
                .creator(creator)
                .mode(mode)
                .theme(theme)
                .difficulty(difficulty)
                .deadline(deadlineDays != null ? LocalDateTime.now().plusDays(deadlineDays) : LocalDateTime.now().plusDays(1))
                .status(CaseStatus.QUEUED)
                .createdAt(LocalDateTime.now())
                .participants(new java.util.HashSet<>())
                .build();

        newCase.getParticipants().add(creator);
        newCase = caseRepository.save(newCase);

        processGeneration(newCase.getId(), cluesCount);

        return newCase;
    }

    @Async
    public void processGeneration(Long caseId, Integer cluesCount) {
        DetectiveCase detCase = caseRepository.findById(caseId).orElseThrow(() -> new IllegalArgumentException("Case not found"));

        detCase.setStatus(CaseStatus.GENERATING);
        caseRepository.save(detCase);

        try {
            java.util.Map<String, Object> aiResponse = aiGenerationService.generateCaseTemplate(detCase.getTheme(), detCase.getDifficulty(), cluesCount);
            detCase.setTitle((String) aiResponse.get("title"));
            detCase.setIntro((String) aiResponse.get("intro"));
            detCase.setGroundTruth((String) aiResponse.get("groundTruth"));

            if (aiResponse.containsKey("tags") && aiResponse.get("tags") != null) {
                java.util.List<String> rawTags = (java.util.List<String>) aiResponse.get("tags");
                detCase.setTags(rawTags);
            }

            java.util.List<java.util.Map<String, Object>> rawSuspects = 
                (java.util.List<java.util.Map<String, Object>>) aiResponse.get("suspects");

            java.util.List<com.detective.backend.domain.Suspect> caseSuspects = new java.util.ArrayList<>();
            if (rawSuspects != null) {
                for (java.util.Map<String, Object> rawSuspect : rawSuspects) {
                    com.detective.backend.domain.Suspect s = new com.detective.backend.domain.Suspect();
                    s.setName((String) rawSuspect.get("name"));
                    s.setDescription((String) rawSuspect.get("description"));
                    s.setAlibi((String) rawSuspect.get("alibi"));
                    s.setDetectiveCase(detCase);
                    caseSuspects.add(s);
                }
            }
            detCase.setSuspects(caseSuspects);

            java.util.List<java.util.Map<String, Object>> rawClues =
                (java.util.List<java.util.Map<String, Object>>) aiResponse.get("clues");
                
            java.util.List<com.detective.backend.domain.Clue> caseClues = new java.util.ArrayList<>();
            if (rawClues != null) {
                for (java.util.Map<String, Object> rawClue : rawClues) {
                    com.detective.backend.domain.Clue c = new com.detective.backend.domain.Clue();
                    c.setContent((String) rawClue.get("content"));
                    c.setType((String) rawClue.get("type"));

                    Object rank = rawClue.get("importanceRank");
                    if (rank instanceof Integer) {
                        c.setImportanceRank((Integer) rank);
                    } else if (rank instanceof String) {
                        try {
                            c.setImportanceRank(Integer.parseInt((String) rank));
                        } catch (Exception ex) {}
                    }
                    if (c.getImportanceRank() == null) {
                        c.setImportanceRank(99);
                    }
                    c.setDetectiveCase(detCase);
                    caseClues.add(c);
                }

                caseClues.sort(java.util.Comparator.comparingInt(com.detective.backend.domain.Clue::getImportanceRank));

                if (detCase.getDeadline() == null) {
                    detCase.setDeadline(LocalDateTime.now().plusDays(1));
                }

                LocalDateTime now = LocalDateTime.now();
                long totalMinutes = java.time.Duration.between(now, detCase.getDeadline()).toMinutes();

                int totalGenClues = caseClues.size();
                int initialRevealCount = Math.min(2, totalGenClues);

                for (int i = 0; i < totalGenClues; i++) {
                    com.detective.backend.domain.Clue c = caseClues.get(i);
                    if (i < initialRevealCount) {
                        c.setPublished(true);
                        c.setRevealAt(now);
                    } else {
                        c.setPublished(false);
                        int remainingClues = totalGenClues - initialRevealCount;
                        long stepMinutes = totalMinutes / (remainingClues + 1);
                        long offsetMinutes = stepMinutes * (i - initialRevealCount + 1);
                        c.setRevealAt(now.plusMinutes(offsetMinutes));
                    }
                }
            }
            detCase.setClues(caseClues);
            
            detCase.setStatus(CaseStatus.READY);
            caseRepository.save(detCase);
            log.info("Case {} generated successfully.", caseId);

            if (detCase.getMode() == Mode.PUBLIC) {
                messagingTemplate.convertAndSend("/topic/cases", detCase);
            }
        } catch (Exception e) {
            log.error("Failed to generate case {}", caseId, e);
            detCase.setStatus(CaseStatus.FAILED);
            caseRepository.save(detCase);
        }
    }
}
