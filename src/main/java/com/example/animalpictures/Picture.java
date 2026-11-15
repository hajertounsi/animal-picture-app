package com.example.animalpictures;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pictures")
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalType animalType;

    @Column(nullable = false, length = 5_000_000)
    private byte[] data;

    @Column(nullable = false)
    private String contentType;

    private String sourceUrl;

    @Column(nullable = false)
    private Instant createdAt;

    protected Picture() {
    }

    public Picture(AnimalType animalType, byte[] data, String contentType, String sourceUrl) {
        this.animalType = animalType;
        this.data = data;
        this.contentType = contentType;
        this.sourceUrl = sourceUrl;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AnimalType getAnimalType() { return animalType; }
    public byte[] getData() { return data; }
    public String getContentType() { return contentType; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
