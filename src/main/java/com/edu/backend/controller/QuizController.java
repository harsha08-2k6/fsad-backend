package com.edu.backend.controller;

import com.edu.backend.model.Quiz;
import com.edu.backend.model.Question;
import com.edu.backend.repository.QuizRepository;
import com.edu.backend.repository.ClassRepository;
import com.edu.backend.repository.SubjectRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizRepository quizRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    @GetMapping
    public List<Quiz> getQuizzes(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) String teacherId) {
        if (classId != null && !classId.isEmpty()) {
            return quizRepository.findBySchoolClassId(classId);
        } else if (teacherId != null && !teacherId.isEmpty()) {
            return quizRepository.findByTeacherId(teacherId);
        }
        return quizRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id) {
        return quizRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody QuizCreateRequest req) {
        try {
            Quiz quiz = new Quiz();
            quiz.setTitle(req.getTitle());
            quiz.setDescription(req.getDescription());
            quiz.setTeacherId(req.getTeacherId());
            quiz.setCreatedAt(LocalDateTime.now());

            if (req.getClassId() != null) {
                classRepository.findById(req.getClassId()).ifPresent(quiz::setSchoolClass);
            }
            if (req.getSubjectId() != null) {
                subjectRepository.findById(req.getSubjectId()).ifPresent(quiz::setSubject);
            }

            if (req.getQuestions() != null) {
                List<Question> questions = req.getQuestions().stream().map(qReq -> {
                    Question q = new Question();
                    q.setQuestionText(qReq.getQuestionText());
                    q.setOptions(qReq.getOptions());
                    q.setCorrectOptionIndex(qReq.getCorrectOptionIndex());
                    q.setPoints(qReq.getPoints());
                    q.setQuiz(quiz);
                    return q;
                }).collect(Collectors.toList());
                quiz.setQuestions(questions);
            }

            Quiz saved = quizRepository.save(quiz);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating quiz: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable String id) {
        if (!quizRepository.existsById(id)) return ResponseEntity.notFound().build();
        quizRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

@Data
class QuizCreateRequest {
    private String title;
    private String description;
    private String classId;
    private String subjectId;
    private String teacherId;
    private List<QuestionRequest> questions;
}

@Data
class QuestionRequest {
    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;
    private Integer points;
}
