package com.edu.backend.repository;

import com.edu.backend.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, String> {
}
