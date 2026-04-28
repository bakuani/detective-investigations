package com.detective.backend.repository;

import com.detective.backend.domain.Clue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ClueRepository extends JpaRepository<Clue, Long> {
    long countByIsPublishedTrueAndRevealAtAfter(LocalDateTime time);
    long countByIsPublishedTrueAndRevealAtBetween(LocalDateTime start, LocalDateTime end);
    List<Clue> findByIsPublishedFalseAndRevealAtBefore(LocalDateTime time);
    List<Clue> findByDetectiveCaseIdAndIsPublishedTrueOrderByRevealAtAsc(Long caseId);
}
