package com.detective.backend.controller;

import com.detective.backend.domain.ChatMessage;
import com.detective.backend.domain.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.detective.backend.dto.ChatSendMessageRequest;
import com.detective.backend.repository.ChatMessageRepository;
import com.detective.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping
    public ResponseEntity<List<ChatMessage>> getChatMessages(@PathVariable Long caseId) {
        return ResponseEntity.ok(chatMessageRepository.findByDetectiveCaseIdOrderByCreatedAtAsc(caseId));
    }

    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable Long caseId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatSendMessageRequest request) {
            
        ChatMessage message = chatService.sendUserMessage(caseId, user.getId(), request.getContent());
        return ResponseEntity.ok(message);
    }
}
