package com.edu.backend.controller;

import com.edu.backend.model.Topic;
import com.edu.backend.repository.SubjectRepository;
import com.edu.backend.repository.TopicRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TopicController {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    @GetMapping
    public List<Topic> getTopics(@RequestParam(required = false) String subjectId) {
        if (subjectId != null) {
            return topicRepository.findBySubjectId(subjectId);
        }
        return topicRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createTopic(@RequestBody TopicRequest request) {
        try {
            Topic topic = new Topic();
            topic.setName(request.getName());
            subjectRepository.findById(request.getSubject()).ifPresent(topic::setSubject);
            Topic saved = topicRepository.save(topic);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable String id) {
        if (!topicRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        topicRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

@Data
class TopicRequest {
    private String name;
    private String subject;
}
