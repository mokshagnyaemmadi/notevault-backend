package com.example.notevault.controller;

import com.example.notevault.dto.DailyCountDTO;
import com.example.notevault.dto.NoteAdminViewDTO;
import com.example.notevault.model.Note;
import com.example.notevault.model.User;
import com.example.notevault.payload.response.MessageResponse;
import com.example.notevault.repository.NoteRepository;
import com.example.notevault.repository.UserRepository;
import com.example.notevault.security.services.UserDetailsImpl;
import com.example.notevault.service.NoteService;
import com.example.notevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    // Endpoint for dashboard stats card
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        // Log authentication details (for debugging, can be removed)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("--- Inside AdminController /stats ---");
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("-----------------------------------");

        long userCount = userRepository.count();
        long noteCount = noteRepository.count();
        return ResponseEntity.ok(Map.of("totalUsers", userCount, "totalNotes", noteCount));
    }

    // Endpoint to get all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null)); // Hide password
        return ResponseEntity.ok(users);
    }

    // Endpoint to update a user's roles
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long userId, @Valid @RequestBody Set<String> roles) {
        try {
            User updatedUser = userService.updateUserRoles(userId, roles);
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Endpoint to delete a user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
         if (currentUser.getId().equals(userId)) {
             return ResponseEntity.badRequest().body(new MessageResponse("Error: Cannot delete your own account."));
         }
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Endpoint to disable a user
    @PutMapping("/users/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        if (currentUser.getId().equals(userId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cannot disable your own account."));
        }
        try {
            User updatedUser = userService.disableUser(userId);
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Endpoint to enable a user
    @PutMapping("/users/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            User updatedUser = userService.enableUser(userId);
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // Endpoint to get all notes for a specific user
    @GetMapping("/users/{userId}/notes")
    public ResponseEntity<List<Note>> getUserNotes(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        List<Note> notes = noteService.getNotesForUser(userId);
        return ResponseEntity.ok(notes);
    }

    // Endpoint to get all notes in the system
    @GetMapping("/notes")
    public ResponseEntity<List<NoteAdminViewDTO>> getAllNotes() {
        List<Note> allNotes = noteRepository.findAll();
        List<NoteAdminViewDTO> noteDTOs = allNotes.stream()
                .map(NoteAdminViewDTO::fromNote)
                .collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    // Endpoint to delete any note by its ID
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<?> deleteNoteById(@PathVariable Long noteId) {
        try {
            noteService.adminDeleteNote(noteId);
            return ResponseEntity.ok(new MessageResponse("Note deleted successfully by admin!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error deleting note: " + e.getMessage()));
        }
    }

    // Endpoint to get daily user registration stats
    @GetMapping("/stats/registrations")
    public ResponseEntity<List<DailyCountDTO>> getRegistrationStats() {
        Instant startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> results = userRepository.countUsersByDate(startDate);
        List<DailyCountDTO> dtoList = results.stream()
                .map(DailyCountDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Endpoint to get daily new note stats
    @GetMapping("/stats/notes")
    public ResponseEntity<List<DailyCountDTO>> getNoteStats() {
        Instant startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> results = noteRepository.countNotesByDate(startDate);
        List<DailyCountDTO> dtoList = results.stream()
                .map(DailyCountDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}