package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "submission")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime submittedAt = LocalDateTime.now();

    private String fileUrl;

    private Integer grade;

    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    // "graded" or "submitted"
    private String status = "submitted";

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"schoolClass", "password"})
    private User student;
}
