package com.edu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    private final String UPLOAD_DIR = new File("uploads").getAbsolutePath() + File.separator;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Sanitize filename: replace spaces with underscores to avoid URL encoding issues
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().replace(" ", "_") : "file";
            String fileName = System.currentTimeMillis() + "_" + originalName;
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());

            // Get current backend URL from environment or fallback
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null || backendUrl.isEmpty()) {
                backendUrl = "https://fsad-backend-1-ca45.onrender.com"; // Your Render Backend URL
            }
            // Remove trailing slash if present
            if (backendUrl.endsWith("/")) {
                backendUrl = backendUrl.substring(0, backendUrl.length() - 1);
            }

            Map<String, String> response = new HashMap<>();
            response.put("url", backendUrl + "/api/upload/files/" + fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        try {
            // Remove any unwanted characters or backtracking and look in the absolute path
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            File file = filePath.toFile();
            
            if (file.exists() && file.isFile()) {
                Resource resource = new UrlResource(filePath.toUri());
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("FILE NOT FOUND AT: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
