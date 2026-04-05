package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    private Integer correctOptionIndex; // 0-based index

    private Integer points = 1;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Quiz quiz;
}
