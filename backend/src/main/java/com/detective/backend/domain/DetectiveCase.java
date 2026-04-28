package com.detective.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectiveCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User creator;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status;
    
    private String title;
    private String theme;
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(columnDefinition = "TEXT")
    private String groundTruth;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "case_tags", joinColumns = @JoinColumn(name = "case_id"))
    @Column(name = "tag")
    private java.util.List<String> tags = new java.util.ArrayList<>();

    public String getGroundTruth() {
        if (this.status == CaseStatus.CLOSED) {
            return groundTruth;
        }
        return null;
    }
    
    private String generationId;
    
    private LocalDateTime deadline;
    
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "case_participants",
        joinColumns = @JoinColumn(name = "case_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "detectiveCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"detectiveCase", "hibernateLazyInitializer", "handler"})
    private java.util.List<Clue> clues = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "detectiveCase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"detectiveCase", "hibernateLazyInitializer", "handler"})
    private java.util.List<Suspect> suspects = new java.util.ArrayList<>();
}
