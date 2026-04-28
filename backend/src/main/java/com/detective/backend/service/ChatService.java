package com.detective.backend.service;

import com.detective.backend.domain.ChatMessage;
import com.detective.backend.domain.DetectiveCase;
import com.detective.backend.domain.User;
import com.detective.backend.repository.CaseRepository;
import com.detective.backend.repository.ChatMessageRepository;
import com.detective.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessage sendUserMessage(Long caseId, Long userId, String content) {
        DetectiveCase detCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatMessage message = ChatMessage.builder()
                .detectiveCase(detCase)
                .sender(user)
                .content(content)
                .isSystem(false)
                .createdAt(LocalDateTime.now())
                .build();
                
        ChatMessage saved = chatMessageRepository.save(message);
        
        messagingTemplate.convertAndSend("/topic/case/" + caseId + "/chat", saved);
        
        return saved;
    }
    
    @Transactional
    public ChatMessage sendSystemMessage(DetectiveCase detCase, String content) {
        ChatMessage message = ChatMessage.builder()
                .detectiveCase(detCase)
                .sender(null)
                .content(content)
                .isSystem(true)
                .createdAt(LocalDateTime.now())
                .build();
                
        ChatMessage saved = chatMessageRepository.save(message);
        
        messagingTemplate.convertAndSend("/topic/case/" + detCase.getId() + "/chat", saved);
        
        return saved;
    }
}
