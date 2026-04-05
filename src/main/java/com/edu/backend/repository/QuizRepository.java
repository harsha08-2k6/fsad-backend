package com.edu.backend.repository;

import com.edu.backend.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, String> {
    List<Quiz> findBySchoolClassId(String schoolClassId);
    List<Quiz> findByTeacherId(String teacherId);
}
