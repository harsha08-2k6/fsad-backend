package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mark")
public class Mark {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer marksObtained;
    private Integer maxMarks;
    private String examType;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "student_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"password", "schoolClass"})
    private User student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
