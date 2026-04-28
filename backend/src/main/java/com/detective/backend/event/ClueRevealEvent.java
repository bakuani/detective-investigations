package com.detective.backend.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClueRevealEvent {
    private Long clueId;
    private Long caseId;
    private String content;
}
