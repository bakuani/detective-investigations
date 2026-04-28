package com.detective.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DetectiveCase detectiveCase;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String type;
    
    private Integer importanceRank;
    
    private boolean isPublished;
    
    private LocalDateTime revealAt;
}
