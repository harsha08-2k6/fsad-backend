package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role = "student";
    private String status = "active";

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
}

