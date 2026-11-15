package com.example.animalpictures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PictureService {

    private final AnimalPictureClient client;
    private final PictureRepository repository;

    public PictureService(AnimalPictureClient client, PictureRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public List<Picture> fetchAndSave(AnimalType animalType, int count) {
        List<Picture> pictures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            FetchedPicture fetched = client.fetch(animalType);
            pictures.add(new Picture(animalType, fetched.data(), fetched.contentType(), fetched.sourceUrl()));
        }

        return repository.saveAll(pictures);
    }

    public Optional<Picture> findLatest(AnimalType animalType) {
        return repository.findFirstByAnimalTypeOrderByIdDesc(animalType);
    }
}
