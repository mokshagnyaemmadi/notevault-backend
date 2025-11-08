package com.example.notevault.service;

import com.example.notevault.dto.NoteDetailDTO;
import com.example.notevault.model.Note;
import com.example.notevault.model.NoteModificationTimestamp;
import com.example.notevault.model.User;
import com.example.notevault.payload.request.NoteRequest;
import com.example.notevault.repository.NoteRepository;
import com.example.notevault.repository.UserRepository;
import org.owasp.html.PolicyFactory; // Changed import
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService; 

    // --- REVERTED TO BASIC, STABLE POLICY ---
    private final PolicyFactory htmlPolicy = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.IMAGES)
        .and(Sanitizers.LINKS);
    // --- END REVERTED POLICY ---

    @Transactional
    public Note createNote(Long userId, NoteRequest noteRequest, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        Note note = new Note();
        note.setUser(user);
        note.setTitle(noteRequest.getTitle());
        note.setContent(htmlPolicy.sanitize(noteRequest.getContent()));
        note.setTags(noteRequest.getTags());
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/files/") 
                .path(fileName)
                .toUriString();
            note.setAttachmentFilename(file.getOriginalFilename());
            note.setAttachmentUrl(fileDownloadUri);
        }

        NoteModificationTimestamp initialTimestamp = new NoteModificationTimestamp(note);
        note.getModifications().add(initialTimestamp);

        return noteRepository.save(note);
    }
    
    // ... rest of methods remain unchanged
    @Transactional
    public Note updateNote(Long userId, Long noteId, NoteRequest noteRequest, MultipartFile file) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Error: Note not found."));
        if (!Objects.equals(note.getUser().getId(), userId)) {
            throw new AccessDeniedException("You do not have permission to edit this note.");
        }
        note.setTitle(noteRequest.getTitle());
        note.setContent(htmlPolicy.sanitize(noteRequest.getContent()));
        note.setTags(noteRequest.getTags());
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/files/") 
                .path(fileName)
                .toUriString();
            note.setAttachmentFilename(file.getOriginalFilename());
            note.setAttachmentUrl(fileDownloadUri);
        }

        NoteModificationTimestamp updateTimestamp = new NoteModificationTimestamp(note);
        note.getModifications().add(updateTimestamp);

        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public NoteDetailDTO getNoteDetails(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId)
             .orElseThrow(() -> new RuntimeException("Error: Note not found."));

        if (!Objects.equals(note.getUser().getId(), userId)) {
             throw new AccessDeniedException("You do not have permission to view this note's details.");
        }
        note.getModifications().size();
        return NoteDetailDTO.fromNote(note);
    }

    public List<Note> getNotesForUser(Long userId) {
        return noteRepository.findByUserIdOrderByPinnedDescUpdatedAtDesc(userId);
    }

    public List<Note> searchUserNotes(Long userId, String query, String tag) {
        String effectiveQuery = (query != null && !query.trim().isEmpty()) ? query : null;
        String effectiveTag = (tag != null && !tag.trim().isEmpty()) ? tag : null;
        
        if (effectiveQuery == null && effectiveTag == null) {
            return noteRepository.findByUserIdOrderByPinnedDescUpdatedAtDesc(userId);
        }
        
        return noteRepository.searchNotesByQueryAndTag(userId, effectiveQuery, effectiveTag);
    }

    public long countUserNotes(Long userId) {
        return noteRepository.countByUserId(userId);
    }

    @Transactional
    public Note togglePinStatus(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Error: Note not found."));
        if (!Objects.equals(note.getUser().getId(), userId)) {
            throw new AccessDeniedException("You do not have permission to modify this note.");
        }
        note.setPinned(!note.isPinned());
        return noteRepository.save(note);
    }

    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Error: Note not found."));
        if (!Objects.equals(note.getUser().getId(), userId)) {
            throw new AccessDeniedException("You do not have permission to delete this note.");
        }
        noteRepository.deleteById(noteId);
    }

    @Transactional
    public void adminDeleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Error: Note not found with id: " + noteId));
        noteRepository.delete(note);
    }
}