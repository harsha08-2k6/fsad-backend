package com.edu.backend.controller;

import com.edu.backend.model.Assignment;
import com.edu.backend.model.Submission;
import com.edu.backend.repository.AssignmentRepository;
import com.edu.backend.repository.ClassRepository;
import com.edu.backend.repository.SubmissionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final ClassRepository classRepository;
    private final SubmissionRepository submissionRepository;

    @GetMapping
    public List<Assignment> getAllAssignments(@RequestParam(required = false) String classId) {
        if (classId != null && !classId.isEmpty()) {
            return assignmentRepository.findBySchoolClassId(classId);
        }
        return assignmentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignment(@PathVariable String id) {
        return assignmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentCreateRequest req) {
        Assignment assignment = new Assignment();
        assignment.setTitle(req.getTitle());
        assignment.setDescription(req.getDescription());
        assignment.setDeadline(req.getDeadline());
        assignment.setFileUrl(req.getFileUrl());
        if (req.getClassId() != null && !req.getClassId().isEmpty()) {
            classRepository.findById(req.getClassId()).ifPresent(assignment::setSchoolClass);
        }
        Assignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable String id, @RequestBody Assignment updatedDetails) {
        return assignmentRepository.findById(id).map(assignment -> {
            assignment.setTitle(updatedDetails.getTitle());
            assignment.setDescription(updatedDetails.getDescription());
            assignment.setDeadline(updatedDetails.getDeadline());
            assignment.setFileUrl(updatedDetails.getFileUrl());
            assignmentRepository.save(assignment);
            return ResponseEntity.ok(assignment);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @jakarta.transaction.Transactional
    public ResponseEntity<?> deleteAssignment(@PathVariable String id) {
        if (!assignmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // delete all associated submissions first
        List<Submission> submissions = submissionRepository.findByAssignmentId(id);
        if (!submissions.isEmpty()) {
            submissionRepository.deleteAll(submissions);
        }

        assignmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

@Data
class AssignmentCreateRequest {
    private String title;
    private String description;
    private java.time.LocalDateTime deadline;
    private String fileUrl;
    private String classId;
}
