package com.edu.backend.repository;

import com.edu.backend.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, String> {
    List<Topic> findBySubjectId(String subjectId);
}
