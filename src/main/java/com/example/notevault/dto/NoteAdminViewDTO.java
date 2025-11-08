package com.example.notevault.dto;

import com.example.notevault.model.Note;
import java.time.Instant;

// DTO (Data Transfer Object) for displaying notes in the admin view
public class NoteAdminViewDTO {

    private final Long id;
    private final String title;
    private final String contentPreview;
    private final String tags;
    private final String attachmentFilename;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long userId;
    private final String username;

    // Private constructor - use the static factory method
    private NoteAdminViewDTO(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        // Generate a preview (e.g., first 100 chars, strip HTML)
        String content = note.getContent() != null ? note.getContent().replaceAll("<[^>]*>", "") : ""; // Basic HTML strip
        this.contentPreview = content.substring(0, Math.min(content.length(), 100)) + (content.length() > 100 ? "..." : "");
        this.tags = note.getTags();
        this.attachmentFilename = note.getAttachmentFilename();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
        if (note.getUser() != null) {
            this.userId = note.getUser().getId();
            this.username = note.getUser().getUsername();
        } else {
            this.userId = null;
            this.username = "[User Not Available]";
        }
    }

    // Static factory method to create DTO from Note entity
    public static NoteAdminViewDTO fromNote(Note note) {
        return new NoteAdminViewDTO(note);
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContentPreview() { return contentPreview; }
    public String getTags() { return tags; }
    public String getAttachmentFilename() { return attachmentFilename; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
}