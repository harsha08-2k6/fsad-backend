package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "subject")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String code;

    private String description;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "subjects")
    private List<SchoolClass> classes;
}
