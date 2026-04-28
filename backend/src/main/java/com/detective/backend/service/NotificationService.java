package com.detective.backend.service;

import com.detective.backend.domain.User;
import com.detective.backend.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(User user, String title, String message, String type) {
        if (user == null || user.getId() == null) return;
        
        NotificationResponse notification = NotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(type)
                .date("РўРѕР»СЊРєРѕ С‡С‚Рѕ")
                .build();
                
        String destination = "/topic/user/" + user.getId();
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent WS notification to user {}", user.getId());
    }

    public void notifyAll(Set<User> participants, String title, String message, String type) {
        if (participants == null) return;
        for (User user : participants) {
            sendToUser(user, title, message, type);
        }
    }
}
