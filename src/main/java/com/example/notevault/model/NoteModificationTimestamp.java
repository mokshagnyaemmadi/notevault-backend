package com.example.notevault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "note_modifications")
public class NoteModificationTimestamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp // Automatically set when this entity is created
    @Column(nullable = false, updatable = false)
    private Instant modifiedAt;

    @JsonIgnore // Avoid infinite loops when serializing
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    // Constructors
    public NoteModificationTimestamp() {}

    public NoteModificationTimestamp(Note note) {
        this.note = note;
        // modifiedAt will be set automatically by @CreationTimestamp
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Instant modifiedAt) { this.modifiedAt = modifiedAt; }
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
}