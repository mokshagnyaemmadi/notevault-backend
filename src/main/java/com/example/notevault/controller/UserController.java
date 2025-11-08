package com.example.notevault.controller;

import com.example.notevault.payload.request.ChangePasswordRequest;
import com.example.notevault.payload.response.MessageResponse;
import com.example.notevault.security.services.UserDetailsImpl;
import com.example.notevault.service.NoteService;
import com.example.notevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NoteService noteService;

    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.changePassword(getUserIdFromAuthentication(), changePasswordRequest);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        Long userId = getUserIdFromAuthentication();
        long noteCount = noteService.countUserNotes(userId);
        return ResponseEntity.ok(Map.of("noteCount", noteCount));
    }
}