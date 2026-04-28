package com.edu.backend.controller;

import com.edu.backend.model.*;
import com.edu.backend.repository.*;
import com.edu.backend.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final EmailService emailService;

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            List<User> allUsers = userRepository.findAll();
            long total = allUsers.size();
            long teachers = allUsers.stream().filter(u -> "teacher".equals(u.getRole())).count();
            long students = allUsers.stream().filter(u -> "student".equals(u.getRole())).count();
            long classes = classRepository.count();
            long subjects = subjectRepository.count();
            long assignments = assignmentRepository.count();
            long announcements = announcementRepository.count();

            long pendingTeachers = allUsers.stream()
                    .filter(u -> "teacher".equals(u.getRole()) && "pending".equals(u.getStatus())).count();
            long pendingAdmins = allUsers.stream()
                    .filter(u -> "admin".equals(u.getRole()) && "pending".equals(u.getStatus())).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("users", total);
            stats.put("teachers", teachers);
            stats.put("students", students);
            stats.put("classes", classes);
            stats.put("subjects", subjects);
            stats.put("assignments", assignments);
            stats.put("announcements", announcements);
            stats.put("pendingTeachers", pendingTeachers);
            stats.put("pendingAdmins", pendingAdmins);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toUserDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("name", user.getName());
        dto.put("email", user.getEmail());
        dto.put("role", user.getRole());
        dto.put("status", user.getStatus());
        dto.put("classId", user.getSchoolClass() == null ? null : user.getSchoolClass().getId());
        dto.put("teacherId", user.getTeacher() == null ? null : user.getTeacher().getId());
        return dto;
    }

    @PatchMapping("/users")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserUpdateRequest request) {
        return userRepository.findById(request.getUserId()).map(user -> {
            if (request.getName() != null) user.setName(request.getName());
            if (request.getEmail() != null) user.setEmail(request.getEmail());
            if (request.getRole() != null) user.setRole(request.getRole());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(request.getPassword());
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null) return ResponseEntity.badRequest().body("userId is required");

        return userRepository.findById(userId).map(user -> {
            List<User> linked = userRepository.findByTeacherId(userId);
            if (!linked.isEmpty()) {
                linked.forEach(u -> u.setTeacher(null));
                userRepository.saveAll(linked);
            }

            userRepository.deleteById(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teachers/pending")
    public List<User> getPendingTeachers() {
        return userRepository.findAll().stream()
                .filter(u -> "teacher".equals(u.getRole()) && "pending".equals(u.getStatus()))
                .toList();
    }

    @GetMapping("/admins/pending")
    public List<User> getPendingAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()) && "pending".equals(u.getStatus()))
                .toList();
    }

    @PostMapping("/approve-user")
    public ResponseEntity<?> approveUser(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        return userRepository.findById(userId).map(user -> {
            user.setStatus("active");
            User saved = userRepository.save(user);
            try {
                emailService.sendApprovalMail(saved.getEmail(), saved.getRole(), saved.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseEntity.ok(Map.of("message", "User approved successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class UserUpdateRequest {
        private String userId;
        private String name;
        private String email;
        private String role;
        private String password;
    }
}
