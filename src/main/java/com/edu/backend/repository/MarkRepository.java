package com.edu.backend.repository;

import com.edu.backend.model.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, String> {
    List<Mark> findByStudentId(String studentId);
    List<Mark> findBySubjectId(String subjectId);
}
