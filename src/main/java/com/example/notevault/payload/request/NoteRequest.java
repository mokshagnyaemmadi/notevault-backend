package com.example.notevault.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {
    @NotBlank
    private String title;

    private String content;

    private String tags;
}