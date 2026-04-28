package com.detective.backend.service;

import com.detective.backend.domain.Clue;
import com.detective.backend.event.ClueRevealEvent;
import com.detective.backend.repository.ClueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClueKafkaConsumer {

    private final ClueRepository clueRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${app.kafka.topics.clue-reveals}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeClueReveal(ConsumerRecord<String, ClueRevealEvent> record) {
        ClueRevealEvent event = record.value();
        log.info("Received ClueRevealEvent: {}", event);

        clueRepository.findById(event.getClueId()).ifPresent(clue -> {
            if (!clue.isPublished()) {
                clue.setPublished(true);
                clueRepository.save(clue);
                log.info("Clue {} successfully published. FR-035: Notify users in chat.", clue.getId());
                
                String message = "РћС‚РєСЂС‹С‚Р° РЅРѕРІР°СЏ СѓР»РёРєР° СѓСЂРѕРІРЅСЏ РІР°Р¶РЅРѕСЃС‚Рё " + clue.getImportanceRank() + "!";
                chatService.sendSystemMessage(
                        clue.getDetectiveCase(),
                        message
                );
                
                notificationService.notifyAll(
                        clue.getDetectiveCase().getParticipants(),
                        "РќРѕРІР°СЏ СѓР»РёРєР°",
                        "Р’ РґРµР»Рµ '" + clue.getDetectiveCase().getTitle() + "' РїРѕСЏРІРёР»Р°СЃСЊ РЅРѕРІР°СЏ СѓР»РёРєР°!",
                        "info"
                );
                
                messagingTemplate.convertAndSend("/topic/case/" + clue.getDetectiveCase().getId() + "/clues", clue);
            } else {
                log.info("Idempotent check (FR-034): Clue {} is already published.", clue.getId());
            }
        });
    }
}
