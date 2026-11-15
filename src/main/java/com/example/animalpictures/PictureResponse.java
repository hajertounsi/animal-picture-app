package com.example.animalpictures;

import java.time.Instant;

public record PictureResponse(Long id, AnimalType animalType, String sourceUrl, int sizeBytes, Instant createdAt) {

    public static PictureResponse from(Picture picture) {
        return new PictureResponse(
                picture.getId(),
                picture.getAnimalType(),
                picture.getSourceUrl(),
                picture.getData().length,
                picture.getCreatedAt());
    }
}
