package com.edu.backend.controller;

import com.edu.backend.model.Subject;
import com.edu.backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody Subject subject) {
        try {
            Subject saved = subjectRepository.save(subject);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable String id, @RequestBody Subject updated) {
        return subjectRepository.findById(id).map(subject -> {
            subject.setName(updated.getName());
            subject.setCode(updated.getCode());
            subject.setDescription(updated.getDescription());
            return ResponseEntity.ok(subjectRepository.save(subject));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable String id) {
        if (!subjectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        subjectRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
