package com.edu.backend.controller;

import com.edu.backend.model.Mark;
import com.edu.backend.repository.MarkRepository;
import com.edu.backend.repository.SubjectRepository;
import com.edu.backend.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarkController {

    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @GetMapping
    public List<Mark> getAllMarks() {
        return markRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createMark(@RequestBody MarkEntryRequest request) {
        try {
            Mark mark = new Mark();
            mark.setMarksObtained(request.getMarksObtained());
            mark.setMaxMarks(request.getMaxMarks());
            mark.setExamType(request.getExamType());
            
            userRepository.findById(request.getStudentId()).ifPresent(mark::setStudent);
            subjectRepository.findById(request.getSubjectId()).ifPresent(mark::setSubject);
            
            Mark saved = markRepository.save(mark);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public List<Mark> getStudentMarks(@PathVariable String studentId) {
        return markRepository.findByStudentId(studentId);
    }
}

@Data
class MarkEntryRequest {
    private String studentId;
    private String subjectId;
    private Integer marksObtained;
    private Integer maxMarks;
    private String examType;
}
