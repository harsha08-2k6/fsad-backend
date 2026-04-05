package com.edu.backend.controller;

import com.edu.backend.model.Submission;
import com.edu.backend.repository.SubmissionRepository;
import com.edu.backend.repository.UserRepository;
import com.edu.backend.repository.AssignmentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Submission> getSubmissions(
            @RequestParam(required = false) String assignmentId,
            @RequestParam(required = false) String studentId) {
        if (assignmentId != null && studentId != null) {
            Optional<Submission> submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
            return submission.map(List::of).orElse(List.of());
        } else if (assignmentId != null) {
            return submissionRepository.findByAssignmentId(assignmentId);
        } else if (studentId != null) {
            return submissionRepository.findByStudentId(studentId);
        }
        return submissionRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> submitWork(@RequestBody SubmissionRequest request) {
        try {
            Submission submission = new Submission();
            submission.setFileUrl(request.getFileUrl());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("submitted");

            assignmentRepository.findById(request.getAssignmentId()).ifPresent(submission::setAssignment);
            userRepository.findById(request.getStudentId()).ifPresent(submission::setStudent);

            Submission saved = submissionRepository.save(submission);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubmission(@PathVariable String id, @RequestBody SubmissionRequest request) {
        return submissionRepository.findById(id).map(submission -> {
            if ("graded".equals(submission.getStatus())) {
                return ResponseEntity.badRequest().body("Cannot update a graded submission.");
            }
            submission.setFileUrl(request.getFileUrl());
            submission.setSubmittedAt(LocalDateTime.now());
            return ResponseEntity.ok(submissionRepository.save(submission));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/grade")
    public ResponseEntity<?> gradeSubmission(@RequestBody GradeRequest request) {
        Optional<Submission> submissionOpt = submissionRepository.findById(request.getSubmissionId());
        if (submissionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Submission not found.");
        }

        Submission submission = submissionOpt.get();
        submission.setGrade(request.getGrade());
        submission.setFeedback(request.getFeedback());
        submission.setStatus("graded");
        submissionRepository.save(submission);

        return ResponseEntity.ok(submission);
    }
}

@Data
class SubmissionRequest {
    private String assignmentId;
    private String studentId;
    private String fileUrl;
}

@Data
class GradeRequest {
    private String submissionId;
    private Integer grade;
    private String feedback;
}
