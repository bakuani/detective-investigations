package com.detective.backend.repository;

import com.detective.backend.domain.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
    boolean existsByDetectiveCaseIdAndUserId(Long caseId, Long userId);
    List<Solution> findByDetectiveCaseId(Long caseId);
    List<Solution> findByUserId(Long userId);
}
