package com.example.notevault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @JsonIgnore // Don't expose the server file path in the API
    private String filePath;

    // URL that the frontend will use to access the image
    private String url;
}