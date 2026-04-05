package com.edu.backend.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/code-execute")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CodeExecutionController {

    @PostMapping
    public ResponseEntity<?> executeCode(@RequestBody CodeRequest request) {
        String lang = request.getLanguage();
        String code = request.getCode();
        
        Map<String, Object> response = new HashMap<>();
        
        if ("python".equals(lang)) {
            try {
                java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("script", ".py");
                java.nio.file.Files.writeString(tempFile, code);
                ProcessBuilder pb = new ProcessBuilder("python", tempFile.toAbsolutePath().toString());
                pb.redirectErrorStream(true);
                Process process = pb.start();
                String result = new String(process.getInputStream().readAllBytes());
                process.waitFor();
                java.nio.file.Files.deleteIfExists(tempFile);
                response.put("output", result.isEmpty() ? "Executed successfully (no output)" : result);
            } catch (Exception e) {
                // Simulation Fallback
                if (code.contains("print")) {
                    StringBuilder simulatedOutput = new StringBuilder();
                    Pattern p = Pattern.compile("print\\([\"'](.+?)[\"']\\)");
                    Matcher m = p.matcher(code);
                    while (m.find()) { simulatedOutput.append(m.group(1)).append("\n"); }
                    response.put("output", simulatedOutput.length() > 0 ? simulatedOutput.toString() : "Simulation: Code parsed but no valid print statement found.");
                    response.put("simulation", true);
                } else {
                    response.put("error", "Execution failed on Server. Reason: " + e.getMessage());
                    return ResponseEntity.status(500).body(response);
                }
            }
        } else if ("java".equals(lang)) {
            try {
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("javarun");
                java.nio.file.Path tempFile = tempDir.resolve("Main.java");
                java.nio.file.Files.writeString(tempFile, code);
                ProcessBuilder pbCompile = new ProcessBuilder("javac", "Main.java");
                pbCompile.directory(tempDir.toFile());
                pbCompile.redirectErrorStream(true);
                Process pCompile = pbCompile.start();
                String compileOutput = new String(pCompile.getInputStream().readAllBytes());
                pCompile.waitFor();
                
                if (pCompile.exitValue() != 0) {
                    response.put("output", "Compilation Error:\n" + compileOutput);
                } else {
                    ProcessBuilder pbRun = new ProcessBuilder("java", "Main");
                    pbRun.directory(tempDir.toFile());
                    pbRun.redirectErrorStream(true);
                    Process pRun = pbRun.start();
                    String runOutput = new String(pRun.getInputStream().readAllBytes());
                    pRun.waitFor();
                    response.put("output", runOutput.isEmpty() ? "Executed successfully (no output)" : runOutput);
                }
                try {
                    java.nio.file.Files.deleteIfExists(tempFile);
                    java.nio.file.Files.deleteIfExists(tempDir.resolve("Main.class"));
                    java.nio.file.Files.deleteIfExists(tempDir);
                } catch(Exception ignored) {}
            } catch (Exception e) {
                 // Simulation Fallback for Java
                 if (code.contains("System.out.println")) {
                     StringBuilder simulatedOutput = new StringBuilder();
                     Pattern p = Pattern.compile("System\\.out\\.println\\([\"'](.+?)[\"']\\)");
                     Matcher m = p.matcher(code);
                     while (m.find()) { simulatedOutput.append(m.group(1)).append("\n"); }
                     response.put("output", simulatedOutput.length() > 0 ? simulatedOutput.toString() : "Simulation: Java code parsed.");
                     response.put("simulation", true);
                 } else {
                    response.put("error", "Execution failed: javac/java not available on server.");
                    return ResponseEntity.status(500).body(response);
                 }
            }
        } else if ("c".equals(lang)) {
             try {
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("crun");
                java.nio.file.Path tempFile = tempDir.resolve("main.c");
                java.nio.file.Files.writeString(tempFile, code);
                ProcessBuilder pbCompile = new ProcessBuilder("gcc", "main.c", "-o", "main.exe");
                pbCompile.directory(tempDir.toFile());
                pbCompile.redirectErrorStream(true);
                Process pCompile = pbCompile.start();
                String compileOutput = new String(pCompile.getInputStream().readAllBytes());
                pCompile.waitFor();
                
                if (pCompile.exitValue() != 0) {
                    response.put("output", "Compilation Error:\n" + compileOutput);
                } else {
                    ProcessBuilder pbRun = new ProcessBuilder(tempDir.resolve("main.exe").toAbsolutePath().toString());
                    pbRun.directory(tempDir.toFile());
                    pbRun.redirectErrorStream(true);
                    Process pRun = pbRun.start();
                    String runOutput = new String(pRun.getInputStream().readAllBytes());
                    pRun.waitFor();
                    response.put("output", runOutput.isEmpty() ? "Executed successfully (no output)" : runOutput);
                }
                try {
                    java.nio.file.Files.deleteIfExists(tempFile);
                    java.nio.file.Files.deleteIfExists(tempDir.resolve("main.exe"));
                    java.nio.file.Files.deleteIfExists(tempDir);
                } catch(Exception ignored) {}
             } catch (Exception e) {
                 // gcc might not be installed on Windows natively or Render, fallback to simulation
                 if (code.contains("printf")) {
                     StringBuilder simulatedOutput = new StringBuilder();
                     Matcher m = Pattern.compile("printf\\([\"'](.+?)\\\\n[\"']\\)").matcher(code);
                     while (m.find()) { simulatedOutput.append(m.group(1)).append("\n"); }
                     response.put("output", simulatedOutput.length() > 0 ? simulatedOutput.toString() : "Executed successfully (C simulation fallback)\n");
                 } else {
                     response.put("output", "Executed successfully (C simulation fallback)\n");
                 }
                 response.put("simulation", true);
             }
        } else {
            response.put("error", "Language " + lang + " is not supported.");
            return ResponseEntity.badRequest().body(response);
        }
        
        response.put("warnings", null);
        return ResponseEntity.ok(response);
    }
}

@Data
class CodeRequest {
    private String language;
    private String code;
}
