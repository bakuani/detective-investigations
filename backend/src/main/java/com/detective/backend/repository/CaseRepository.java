package com.detective.backend.repository;

import com.detective.backend.domain.CaseStatus;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.Mode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CaseRepository extends JpaRepository<DetectiveCase, Long> {
    long countByStatus(CaseStatus status);
    List<DetectiveCase> findAllByStatusAndDeadlineBefore(CaseStatus status, LocalDateTime deadline);

    List<DetectiveCase> findByMode(Mode mode);

    List<DetectiveCase> findByModeOrCreatorId(Mode mode, Long creatorId);
    
    default List<DetectiveCase> findAllPublicOrUserCases(Long userId) {
        return findByModeOrCreatorId(Mode.PUBLIC, userId);
    }
}
