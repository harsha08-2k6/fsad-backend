package com.edu.backend.controller;

import com.edu.backend.model.Announcement;
import com.edu.backend.repository.AnnouncementRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    @GetMapping
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody AnnouncementRequest request) {
        try {
            Announcement announcement = new Announcement();
            announcement.setTitle(request.getTitle());
            announcement.setContent(request.getContent());
            // frontend sends 'target', model stores as 'targetRole'
            announcement.setTargetRole(request.getTarget() != null ? request.getTarget() : request.getTargetRole());
            announcement.setCreatedAt(LocalDateTime.now());
            Announcement saved = announcementRepository.save(announcement);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnouncement(@PathVariable String id, @RequestBody AnnouncementRequest request) {
        return announcementRepository.findById(id).map(ann -> {
            ann.setTitle(request.getTitle());
            ann.setContent(request.getContent());
            ann.setTargetRole(request.getTarget() != null ? request.getTarget() : request.getTargetRole());
            return ResponseEntity.ok(announcementRepository.save(ann));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable String id) {
        if (!announcementRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        announcementRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

@Data
class AnnouncementRequest {
    private String title;
    private String content;
    private String target;       // from frontend
    private String targetRole;   // fallback alias
}
