package com.detective.backend.controller;

import com.detective.backend.domain.Mode;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.detective.backend.dto.CaseCreateRequest;
import com.detective.backend.domain.Clue;
import com.detective.backend.repository.ClueRepository;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.UserRepository;
import com.detective.backend.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ClueRepository clueRepository;

    @PostMapping
    public ResponseEntity<DetectiveCase> createCase(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CaseCreateRequest request) {
            
        DetectiveCase newCase = caseService.requestCaseGeneration(
                user,
                request.getMode(),
                request.getTheme(),
                request.getDifficulty(),
                request.getCluesCount(),
                request.getDeadlineDays()
        );

        return ResponseEntity.ok(newCase);
    }

    @GetMapping
    public ResponseEntity<List<DetectiveCase>> getAllCases(@AuthenticationPrincipal User user) {
        if (user != null) {
            return ResponseEntity.ok(caseRepository.findAllPublicOrUserCases(user.getId()));
        }
        return ResponseEntity.ok(caseRepository.findByMode(Mode.PUBLIC));
    }

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        long activeCasesCount = caseRepository.countByStatus(com.detective.backend.domain.CaseStatus.READY) + 
                                caseRepository.countByStatus(com.detective.backend.domain.CaseStatus.SCORING);
        long usersCount = userRepository.count();
        long solvedCasesCount = caseRepository.countByStatus(com.detective.backend.domain.CaseStatus.CLOSED);
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long cluesToday = clueRepository.countByIsPublishedTrueAndRevealAtBetween(startOfDay, now);
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("activeCases", activeCasesCount);
        stats.put("detectives", usersCount);
        stats.put("solvedCases", solvedCasesCount);
        stats.put("cluesToday", cluesToday);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetectiveCase> getCaseById(@PathVariable Long id) {
        return caseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/clues")
    public ResponseEntity<List<Clue>> getPublishedClues(@PathVariable Long id) {
        if (!caseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clueRepository.findByDetectiveCaseIdAndIsPublishedTrueOrderByRevealAtAsc(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<DetectiveCase> joinCase(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(caseService.joinCase(user.getId(), id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<DetectiveCase> leaveCase(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(caseService.leaveCase(user.getId(), id));
    }
}
