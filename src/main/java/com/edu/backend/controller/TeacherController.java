package com.edu.backend.controller;

import com.edu.backend.model.User;
import com.edu.backend.repository.UserRepository;
import com.edu.backend.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeacherController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @GetMapping("/api/teacher/students/pending")
    public ResponseEntity<?> getPendingStudents() {
        List<User> pending = userRepository.findAll().stream()
                .filter(u -> "student".equals(u.getRole()) && "pending".equals(u.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/api/teacher/students/{id}/approve")
    public ResponseEntity<?> approveStudent(@PathVariable String id) {
        return userRepository.findById(id).map(student -> {
            student.setStatus("active");
            User saved = userRepository.save(student);
            sendApprovalEmail(saved);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/teacher/students/{id}/reject")
    public ResponseEntity<?> rejectStudent(@PathVariable String id) {
        return userRepository.findById(id).map(student -> {
            student.setStatus("rejected");
            User saved = userRepository.save(student);
            sendRejectionEmail(saved);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generic approve/reject endpoint used by frontend pending-students page.
     * POST /api/users/approve with body { userId, action: 'approve'|'reject' }
     */
    @PatchMapping("/api/users/approve")
    public ResponseEntity<?> approveOrRejectUser(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String action = request.get("action");
            if (userId == null) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            return userRepository.findById(userId).map(user -> {
                if ("approve".equals(action)) {
                    user.setStatus("active");
                    sendApprovalEmail(user);
                } else if ("reject".equals(action)) {
                    user.setStatus("rejected");
                    sendRejectionEmail(user);
                }
                User saved = userRepository.save(user);
                Map<String, Object> response = new HashMap<>();
                response.put("id", saved.getId());
                response.put("status", saved.getStatus());
                return ResponseEntity.ok((Object) response);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(500).body(sw.toString());
        }
    }

    private void sendApprovalEmail(User user) {
        try {
            emailService.sendApprovalMail(user.getEmail(), user.getRole(), user.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRejectionEmail(User user) {
        try {
            emailService.sendRejectionMail(user.getEmail(), user.getRole());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
