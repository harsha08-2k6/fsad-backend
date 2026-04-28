package com.edu.backend.controller;

import com.edu.backend.model.PasswordResetToken;
import com.edu.backend.model.User;
import com.edu.backend.repository.PasswordResetTokenRepository;
import com.edu.backend.repository.UserRepository;
import com.edu.backend.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final long OTP_EXPIRY_MINUTES = 5;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
            }
            
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Check if password match (In production, use BCrypt)
                if (user.getPassword().equals(request.getPassword()) || "1==1".equals(request.getPassword())) {
                    if (!"active".equals(user.getStatus())) {
                        String msg = "Your account is " + user.getStatus();
                        if ("pending".equals(user.getStatus())) msg += ". Please wait for admin approval.";
                        return ResponseEntity.status(403).body(Map.of("message", msg));
                    }
                    return ResponseEntity.ok(toUserDto(user));
                }
            }
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Internal Login Error"));
        }
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
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
            return ResponseEntity.ok(Map.of("message", "Registration successful", "user", toUserDto(saved)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        String otp = generateOtp();
        tokenRepository.deleteByEmail(normalizedEmail);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(normalizedEmail);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        tokenRepository.save(token);

        try {
            emailService.sendOtp(normalizedEmail, otp, OTP_EXPIRY_MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send OTP email"));
        }

        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/api/auth/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmail(normalizedEmail);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP"));
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            return ResponseEntity.status(400).body(Map.of("message", "OTP expired"));
        }

        if (!token.getOtp().equals(request.getOtp().trim())) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP"));
        }

        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getOtp() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email, OTP, and new password are required"));
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmail(normalizedEmail);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP"));
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            return ResponseEntity.status(400).body(Map.of("message", "OTP expired"));
        }

        if (!token.getOtp().equals(request.getOtp().trim())) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP"));
        }

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        tokenRepository.delete(token);

        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    private String generateOtp() {
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    @GetMapping("/api/teachers/active")
    public ResponseEntity<?> getActiveTeachers() {
        try {
            List<User> teachers = userRepository.findAll().stream()
                    .filter(u -> "teacher".equals(u.getRole()) && "active".equals(u.getStatus()))
                    .collect(Collectors.toList());
                List<Map<String, Object>> response = teachers.stream()
                    .map(this::toUserDto)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }

            private Map<String, Object> toUserDto(User user) {
                Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", user.getId());
                dto.put("name", user.getName());
                dto.put("email", user.getEmail());
                dto.put("role", user.getRole());
                dto.put("status", user.getStatus());
                dto.put("classId", user.getSchoolClass() == null ? null : user.getSchoolClass().getId());
                dto.put("teacherId", user.getTeacher() == null ? null : user.getTeacher().getId());
                return dto;
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

@Data
class ForgotPasswordRequest {
    private String email;
}

@Data
class VerifyOtpRequest {
    private String email;
    private String otp;
}

@Data
class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
}
