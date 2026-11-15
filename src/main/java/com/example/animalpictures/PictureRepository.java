package com.example.animalpictures;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture, Long> {

    Optional<Picture> findFirstByAnimalTypeOrderByIdDesc(AnimalType animalType);
}
