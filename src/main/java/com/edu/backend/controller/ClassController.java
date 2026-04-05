package com.edu.backend.controller;

import com.edu.backend.model.SchoolClass;
import com.edu.backend.model.Subject;
import com.edu.backend.model.User;
import com.edu.backend.repository.ClassRepository;
import com.edu.backend.repository.SubjectRepository;
import com.edu.backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClassController {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<SchoolClass> getAllClasses() {
        return classRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createClass(@RequestBody ClassRequest request) {
        try {
            SchoolClass newClass = new SchoolClass();
            newClass.setName(request.getName());

            if (request.getSubjects() != null) {
                List<Subject> subjects = request.getSubjects().stream()
                        .map(id -> subjectRepository.findById(id).orElse(null))
                        .filter(s -> s != null)
                        .collect(Collectors.toList());
                newClass.setSubjects(subjects);
            }

            SchoolClass savedClass = classRepository.save(newClass);
            return ResponseEntity.ok(savedClass);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{classId}")
    public ResponseEntity<?> updateClass(@PathVariable String classId, @RequestBody ClassRequest request) {
        Optional<SchoolClass> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SchoolClass schoolClass = classOpt.get();
        schoolClass.setName(request.getName());

        if (request.getSubjects() != null) {
            List<Subject> subjects = request.getSubjects().stream()
                    .map(id -> subjectRepository.findById(id).orElse(null))
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
            schoolClass.setSubjects(subjects);
        }
        return ResponseEntity.ok(classRepository.save(schoolClass));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<?> deleteClass(@PathVariable String classId) {
        if (!classRepository.existsById(classId)) {
            return ResponseEntity.notFound().build();
        }
        classRepository.deleteById(classId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{classId}/enroll-bulk")
    public ResponseEntity<?> enrollBulk(@PathVariable String classId, @RequestBody EnrollBulkRequest request) {
        Optional<SchoolClass> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Class not found.");
        }

        SchoolClass schoolClass = classOpt.get();

        if (request.getStudentIds() != null) {
            for (String studentId : request.getStudentIds()) {
                userRepository.findById(studentId).ifPresent(student -> {
                    student.setSchoolClass(schoolClass);
                    userRepository.save(student);
                });
            }
        }

        return ResponseEntity.ok("Students enrolled successfully.");
    }

    @GetMapping("/{classId}/students")
    public ResponseEntity<?> getClassStudents(@PathVariable String classId) {
        Optional<SchoolClass> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(classOpt.get().getStudents());
    }
}

@Data
class ClassRequest {
    private String name;
    private List<String> subjects;
}

@Data
class EnrollBulkRequest {
    private List<String> studentIds;
}
