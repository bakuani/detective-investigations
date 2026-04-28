package com.detective.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suspects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DetectiveCase detectiveCase;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String alibi;
}
