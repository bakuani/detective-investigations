package com.detective.backend.repository;

import com.detective.backend.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByDetectiveCaseIdOrderByCreatedAtAsc(Long caseId);
}
