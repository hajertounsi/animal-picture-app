package com.example.animalpictures;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pictures")
public class PictureController {

    private final PictureService service;

    public PictureController(PictureService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<PictureResponse> fetchAndSave(
            @RequestParam AnimalType animal,
            @RequestParam(defaultValue = "1") @Min(1) @Max(10) int count) {

        return service.fetchAndSave(animal, count).stream()
                .map(PictureResponse::from)
                .toList();
    }

    @GetMapping("/{animal}/latest")
    public ResponseEntity<byte[]> latest(@PathVariable AnimalType animal) {
        Picture picture = service.findLatest(animal)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No " + animal + " picture saved yet"));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(picture.getContentType()))
                .body(picture.getData());
    }
}
