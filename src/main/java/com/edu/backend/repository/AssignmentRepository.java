package com.edu.backend.repository;

import com.edu.backend.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    List<Assignment> findBySchoolClassId(String schoolClassId);
}
