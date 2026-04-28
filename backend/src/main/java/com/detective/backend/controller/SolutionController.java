package com.detective.backend.controller;

import com.detective.backend.domain.CaseStatus;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.SolutionRepository;
import com.detective.backend.domain.Solution;
import com.detective.backend.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.detective.backend.dto.SolutionSubmitRequest;
import com.detective.backend.service.SolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/solutions")
@RequiredArgsConstructor
public class SolutionController {

    private final SolutionService solutionService;
    private final SolutionRepository solutionRepository;
    private final CaseRepository caseRepository;

    @PostMapping
    public ResponseEntity<Solution> submitSolution(
            @PathVariable Long caseId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SolutionSubmitRequest request) {
            
        Solution solution = solutionService.submitSolution(
                caseId,
                user.getId(),
                request.getSuspect(),
                request.getMotive(),
                request.getMethod(),
                request.getJustification()
        );
        return ResponseEntity.ok(solution);
    }
    
    @GetMapping
    public ResponseEntity<List<Solution>> getSolutions(@PathVariable Long caseId, @AuthenticationPrincipal User user) {
        DetectiveCase detCase = caseRepository.findById(caseId).orElseThrow(() -> new IllegalArgumentException("Case not found"));
        
        if (detCase.getStatus() != CaseStatus.CLOSED) {
            return ResponseEntity.ok(solutionRepository.findByDetectiveCaseId(caseId).stream()
                    .filter(s -> s.getUser().getId().equals(user.getId())).toList());
        }
        
        return ResponseEntity.ok(solutionRepository.findByDetectiveCaseId(caseId));
    }
}
