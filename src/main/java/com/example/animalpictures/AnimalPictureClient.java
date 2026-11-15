package com.example.animalpictures;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AnimalPictureClient {

    private final RestClient restClient;

    public AnimalPictureClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public FetchedPicture fetch(AnimalType animalType) {
        String url = buildUrl(animalType);

        ResponseEntity<byte[]> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(byte[].class);

        MediaType contentType = response.getHeaders().getContentType();
        String type = (contentType != null) ? contentType.toString() : "image/jpeg";

        return new FetchedPicture(response.getBody(), type, url);
    }

    private String buildUrl(AnimalType animalType) {
        int width = randomSize();
        int height = randomSize();

        return switch (animalType) {
            case CAT -> "https://cataas.com/cat";
            case DOG -> "https://place.dog/" + width + "/" + height;
            case BEAR -> "https://placebear.com/" + width + "/" + height;
        };
    }

    private int randomSize() {
        return ThreadLocalRandom.current().nextInt(200, 500);
    }
}
