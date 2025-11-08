package com.example.notevault.dto;

import com.example.notevault.model.Note;
import com.example.notevault.model.NoteModificationTimestamp; // Import Timestamp entity

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// DTO for sending full note details including modification history
public class NoteDetailDTO {

    private Long id;
    private String title;
    private String content; // Send full content
    private String tags;
    private String attachmentFilename;
    private String attachmentUrl;
    private Instant createdAt;
    private Instant updatedAt; // Last update time
    private List<Instant> modificationTimestamps; // List of all modification times

    private NoteDetailDTO() {}

    public static NoteDetailDTO fromNote(Note note) {
        NoteDetailDTO dto = new NoteDetailDTO();
        dto.id = note.getId();
        dto.title = note.getTitle();
        dto.content = note.getContent(); // Include full content
        dto.tags = note.getTags();
        dto.attachmentFilename = note.getAttachmentFilename();
        dto.attachmentUrl = note.getAttachmentUrl();
        dto.createdAt = note.getCreatedAt();
        dto.updatedAt = note.getUpdatedAt();

        // Extract and sort timestamps (newest first is often useful)
        if (note.getModifications() != null) {
            dto.modificationTimestamps = note.getModifications().stream()
                    .map(NoteModificationTimestamp::getModifiedAt)
                    .sorted(Instant::compareTo) // Sort ascending (oldest first)
                    // .sorted(Comparator.reverseOrder()) // Or descending (newest first)
                    .collect(Collectors.toList());
        } else {
            dto.modificationTimestamps = List.of(); // Empty list if null
        }

        return dto;
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTags() { return tags; }
    public String getAttachmentFilename() { return attachmentFilename; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Instant> getModificationTimestamps() { return modificationTimestamps; }
}