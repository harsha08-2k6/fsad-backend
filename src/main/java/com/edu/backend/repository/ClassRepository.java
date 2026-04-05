package com.edu.backend.repository;

import com.edu.backend.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassRepository extends JpaRepository<SchoolClass, String> {
    List<SchoolClass> findByTeacherId(String teacherId);
}
