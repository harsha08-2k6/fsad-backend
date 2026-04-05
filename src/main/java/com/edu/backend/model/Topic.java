package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "topic")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("topics")
    private Subject subject;
}
