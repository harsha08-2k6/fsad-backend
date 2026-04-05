package com.edu.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "`class`")
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String teacherId;

    @ManyToMany
    @JoinTable(
        name = "class_subjects",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("schoolClass")
    @OneToMany(mappedBy = "schoolClass")
    private List<User> students;
}
