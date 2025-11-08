package com.example.notevault.controller;

import com.example.notevault.dto.NoteDetailDTO;
import com.example.notevault.model.Note;
import com.example.notevault.payload.request.NoteRequest;
import com.example.notevault.payload.response.MessageResponse;
import com.example.notevault.security.services.UserDetailsImpl;
import com.example.notevault.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
             throw new RuntimeException("User not authenticated properly");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping
    public ResponseEntity<Note> createNote(
            @RequestPart("note") @Valid NoteRequest noteRequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
             return ResponseEntity.ok(noteService.createNote(getUserIdFromAuthentication(), noteRequest, file));
        } catch (RuntimeException e) {
             return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @RequestPart("note") @Valid NoteRequest noteRequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
         try {
            return ResponseEntity.ok(noteService.updateNote(getUserIdFromAuthentication(), id, noteRequest, file));
         } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
         }
    }

    @GetMapping
    public ResponseEntity<List<Note>> getUserNotes() {
         try {
            return ResponseEntity.ok(noteService.getNotesForUser(getUserIdFromAuthentication()));
         } catch (RuntimeException e) {
             return ResponseEntity.status(401).build();
         }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "tag", required = false) String tag) {
        try {
            Long userId = getUserIdFromAuthentication();
            return ResponseEntity.ok(noteService.searchUserNotes(userId, query, tag));
        } catch (RuntimeException e) {
             return ResponseEntity.status(401).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(getUserIdFromAuthentication(), id);
            return ResponseEntity.ok(new MessageResponse("Note deleted successfully!"));
        } catch (RuntimeException e) {
             return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Endpoint to toggle the pin status of a note
    @PutMapping("/{id}/pin")
    public ResponseEntity<?> toggleNotePinStatus(@PathVariable Long id) {
         try {
            Note updatedNote = noteService.togglePinStatus(getUserIdFromAuthentication(), id);
            return ResponseEntity.ok(updatedNote);
         } catch (AccessDeniedException e) {
             return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
         } catch (RuntimeException e) {
             return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
         }
    }

    // Endpoint to get full details including modification history
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getNoteDetails(@PathVariable Long id) {
        try {
            NoteDetailDTO noteDetails = noteService.getNoteDetails(getUserIdFromAuthentication(), id);
            return ResponseEntity.ok(noteDetails);
        } catch (AccessDeniedException e) {
             return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
             return ResponseEntity.notFound().build();
        }
    }
}