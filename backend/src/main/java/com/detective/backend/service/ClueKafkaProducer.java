package com.detective.backend.service;

import com.detective.backend.event.ClueRevealEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClueKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.clue-reveals}")
    private String topicName;

    public void sendClueRevealEvent(ClueRevealEvent event) {
        log.info("Sending ClueRevealEvent to Kafka topic {}: {}", topicName, event);
        kafkaTemplate.send(topicName, event.getClueId().toString(), event);
    }
}
