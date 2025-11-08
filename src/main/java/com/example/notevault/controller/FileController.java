package com.example.notevault.controller;

// Use the renamed FileStorageService
import com.example.notevault.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest; // NEW: Import this
import org.slf4j.Logger; // NEW: Import Logger
import org.slf4j.LoggerFactory; // NEW: Import LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException; // NEW: Import this
import java.util.Map;

@RestController
@RequestMapping("/api/images") // Keep URL path for frontend compatibility
public class FileController { // Renamed the class

    private static final Logger logger = LoggerFactory.getLogger(FileController.class); // Add logger

    @Autowired
    private FileStorageService fileStorageService; // Use the renamed service

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile file) { // Renamed param for clarity, but frontend uses "image"
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/files/")
                .path(fileName)
                .toUriString();
        return ResponseEntity.ok(Map.of("url", fileDownloadUri));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) { // Added HttpServletRequest
        Resource resource = fileStorageService.loadFileAsResource(filename);
        String contentType = "application/octet-stream"; // Default fallback type

        try {
            // Dynamically determine the file's content type
            String detectedType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (detectedType != null) {
                contentType = detectedType;
                logger.info("Detected MIME type for {}: {}", filename, contentType); // Log detected type
            } else {
                 logger.warn("Could not determine MIME type for {}. Falling back to '{}'", filename, contentType); // Log fallback
            }
        } catch (IOException ex) {
            logger.error("Could not get file to determine MIME type for {}: {}", filename, ex.getMessage());
        } catch (NullPointerException ex) {
             logger.error("Null pointer exception while determining MIME type for {}: {}", filename, ex.getMessage());
        }


        // Determine Content-Disposition: 'inline' tries to display, 'attachment' forces download
        String disposition = "inline";

        return ResponseEntity.ok()
                // Use the dynamically determined content type
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}