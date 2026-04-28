package com.detective.backend.service;

import com.detective.backend.domain.Clue;
import com.detective.backend.event.ClueRevealEvent;
import com.detective.backend.repository.ClueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClueScheduler {

    private final ClueRepository clueRepository;
    private final ClueKafkaProducer clueKafkaProducer;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void scheduleClueReveals() {
        LocalDateTime now = LocalDateTime.now();
        List<Clue> cluesToReveal = clueRepository.findByIsPublishedFalseAndRevealAtBefore(now);

        for (Clue clue : cluesToReveal) {
            log.info("FR-033: Emitting clue reveal event for clue: {}", clue.getId());
            clueKafkaProducer.sendClueRevealEvent(new ClueRevealEvent(
                    clue.getId(),
                    clue.getDetectiveCase().getId(),
                    clue.getContent()
            ));
        }
    }
}
