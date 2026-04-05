package com.edu.backend.controller;

import com.edu.backend.model.User;
import com.edu.backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // In a real app, use BCrypt. Working with bypass for now as requested.
                if (user.getPassword().equals(request.getPassword()) || "1==1".equals(request.getPassword())) {
                    if (!"active".equals(user.getStatus())) {
                        return ResponseEntity.status(403).body(Map.of("message", "Your account is " + user.getStatus() + ". Please wait for approval."));
                    }
                    return ResponseEntity.ok(user);
                }
            }
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Login Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        
        // New users are pending by default
        user.setStatus("pending");
        // Set teacher if assignedTeacherId is present (for students)
        if ("student".equals(request.getRole()) && request.getAssignedTeacherId() != null && !request.getAssignedTeacherId().isEmpty()) {
            userRepository.findById(request.getAssignedTeacherId()).ifPresent(user::setTeacher);
        }

        try {
            User saved = userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Registration successful", "user", saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }

    @GetMapping("/api/teachers/active")
    public ResponseEntity<?> getActiveTeachers() {
        try {
            List<User> teachers = userRepository.findAll().stream()
                    .filter(u -> "teacher".equals(u.getRole()) && "active".equals(u.getStatus()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }
}

@Data
class LoginRequest {
    private String email;
    private String password;
}

@Data
class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String assignedTeacherId;
}
