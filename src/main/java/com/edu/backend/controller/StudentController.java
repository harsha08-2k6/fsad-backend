package com.edu.backend.controller;

import com.edu.backend.model.User;
import com.edu.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class StudentController {

    private final UserRepository userRepository;

    @GetMapping
    public List<Map<String, Object>> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(u -> "student".equals(u.getRole()))
                .map(this::toStudentMap)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody User student) {
        if (userRepository.existsByEmail(student.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email already in use.");
            return ResponseEntity.badRequest().body(error);
        }
        student.setRole("student");
        student.setStatus("active");
        return ResponseEntity.ok(toStudentMap(userRepository.save(student)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(student -> {
            student.setName(updatedUser.getName());
            student.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                student.setPassword(updatedUser.getPassword());
            }
            return ResponseEntity.ok(toStudentMap(userRepository.save(student)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingStudents() {
        return userRepository.findAll().stream()
                .filter(u -> "student".equals(u.getRole()) && "pending".equals(u.getStatus()))
                .map(this::toStudentMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toStudentMap(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("email", u.getEmail());
        map.put("role", u.getRole());
        map.put("status", u.getStatus());
        map.put("custom_id", u.getId()); // expose id as custom_id for frontend display
        if (u.getSchoolClass() != null) {
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("id", u.getSchoolClass().getId());
            classInfo.put("name", u.getSchoolClass().getName());
            map.put("class", classInfo);
        } else {
            map.put("class", null);
        }
        return map;
    }
}
