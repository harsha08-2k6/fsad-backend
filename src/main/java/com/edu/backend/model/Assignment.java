package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assignment")
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "class_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("assignments")
    private SchoolClass schoolClass;
}
