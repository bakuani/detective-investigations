package com.detective.backend.service;

import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.Solution;
import com.detective.backend.domain.User;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.SolutionRepository;
import com.detective.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    public Solution submitSolution(Long caseId, Long userId, String suspect, String motive, String method, String justification) {
        
        DetectiveCase detCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
                
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        LocalDateTime now = LocalDateTime.now();
        
        if (detCase.getDeadline() != null && now.isAfter(detCase.getDeadline())) {
            throw new IllegalStateException("Deadline has passed. No further solutions can be submitted. Case closed.");
        }
        
        if (solutionRepository.existsByDetectiveCaseIdAndUserId(caseId, userId)) {
            throw new IllegalStateException("User has already submitted a solution for this case. Only one attempt is allowed.");
        }

        Solution newSolution = Solution.builder()
                .detectiveCase(detCase)
                .user(user)
                .suspect(suspect)
                .motive(motive)
                .method(method)
                .justification(justification)
                .submittedAt(now)
                .build();
                
        solutionRepository.save(newSolution);
        
        log.info("FR-053: User {} successfully submitted solution for case {}. Notify chat.", user.getUsername(), caseId);
        
        chatService.sendSystemMessage(detCase, "РџРѕР»СЊР·РѕРІР°С‚РµР»СЊ " + user.getUsername() + " СЃРґР°Р» СЂРµС€РµРЅРёРµ.");
        
        return newSolution;
    }
}
