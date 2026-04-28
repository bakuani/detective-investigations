package com.detective.backend.service;

import com.detective.backend.domain.CaseStatus;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.Solution;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.SolutionRepository;
import com.detective.backend.domain.User;
import com.detective.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseScoringScheduler {

    private final CaseRepository caseRepository;
    private final SolutionRepository solutionRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredCases() {
        LocalDateTime now = LocalDateTime.now();
        List<DetectiveCase> activeCases = caseRepository.findAllByStatusAndDeadlineBefore(CaseStatus.READY, now);

        for (DetectiveCase detCase : activeCases) {
            log.info("Р”РµРґР»Р°Р№РЅ РґРµР»Р° {} РЅР°СЃС‚СѓРїРёР». Р—Р°РєСЂС‹РІР°РµРј Рё РЅР°С‡РёРЅР°РµРј СЃРєРѕСЂРёРЅРі.", detCase.getId());
            detCase.setStatus(CaseStatus.CLOSED);
            caseRepository.save(detCase);
            
            chatService.sendSystemMessage(detCase, "Р”РµР»Рѕ Р·Р°РєСЂС‹С‚Рѕ. Р РµС€РµРЅРёСЏ Р±РѕР»СЊС€Рµ РЅРµ РїСЂРёРЅРёРјР°СЋС‚СЃСЏ. РћР¶РёРґР°Р№С‚Рµ СЂРµР·СѓР»СЊС‚Р°С‚С‹ СЃРєРѕСЂРёРЅРіР°.");
            
            messagingTemplate.convertAndSend("/topic/case/" + detCase.getId() + "/status", "CLOSED");

            notificationService.notifyAll(
                    detCase.getParticipants(),
                    "Р”РµРґР»Р°Р№РЅ РёСЃС‚С‘Рє",
                    "Р”РµР»Рѕ '" + detCase.getTitle() + "' Р·Р°РєСЂС‹С‚Рѕ, РёРґС‘С‚ СЃРєРѕСЂРёРЅРі СЂРµС€РµРЅРёР№.",
                    "warning"
            );
            
            scoreSolutions(detCase);
        }
    }

    @Async
    protected void scoreSolutions(DetectiveCase detCase) {
        log.info("Р—Р°РїСѓСЃРє AI-СЃРєРѕСЂРёРЅРіР° РґР»СЏ РґРµР»Р° {}", detCase.getId());
        List<Solution> solutions = solutionRepository.findByDetectiveCaseId(detCase.getId());
        
        try {
            Thread.sleep(5000); 
            
            for (Solution solution : solutions) {
                int mockScore = (int) (Math.random() * 100);
                solution.setScore(mockScore);
                solution.setScoreExplanation("AI Mock: Р РµС€РµРЅРёРµ Р»РѕРіРёС‡РЅРѕ, РЅРѕ СѓР»РёРєР° СЃ РЅРѕР¶РѕРј РёРіРЅРѕСЂРёСЂСѓРµС‚СЃСЏ.");
                
                User user = solution.getUser();
                user.setExperience(user.getExperience() + mockScore);
                user.setRating(user.getRating() + (mockScore / 10));
                userRepository.save(user);
            }
            solutionRepository.saveAll(solutions);
            log.info("РЎРєРѕСЂРёРЅРі РґР»СЏ РґРµР»Р° {} Р·Р°РІРµСЂС€РµРЅ. РћС†РµРЅРµРЅРѕ СЂРµС€РµРЅРёР№: {}", detCase.getId(), solutions.size());
            
            chatService.sendSystemMessage(detCase, "РџРѕРґРІРµРґРµРЅС‹ РёС‚РѕРіРё! РСЃС‚РёРЅР° РѕС‚РєСЂС‹С‚Р°. РџРѕСЃРјРѕС‚СЂРёС‚Рµ СЃРІРѕРё СЂРµР·СѓР»СЊС‚Р°С‚С‹.");
            
            messagingTemplate.convertAndSend("/topic/case/" + detCase.getId() + "/status", "SCORED");

            notificationService.notifyAll(
                    detCase.getParticipants(),
                    "Р РµР·СѓР»СЊС‚Р°С‚С‹ РіРѕС‚РѕРІС‹",
                    "Р”Р»СЏ РґРµР»Р° '" + detCase.getTitle() + "' РїРѕРґРІРµРґРµРЅС‹ РёС‚РѕРіРё! РћР·РЅР°РєРѕРјСЊС‚РµСЃСЊ СЃ РѕС†РµРЅРєРѕР№.",
                    "success"
            );
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
