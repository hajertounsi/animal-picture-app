package com.example.animalpictures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class PictureServiceTest {

    private final AnimalPictureClient client = mock(AnimalPictureClient.class);
    private final PictureRepository repository = mock(PictureRepository.class);
    private final PictureService service = new PictureService(client, repository);

    @Test
    void fetchesAndSavesTheRequestedNumberOfPictures() {
        FetchedPicture fakeBear = new FetchedPicture(new byte[] {1, 2, 3}, "image/jpeg", "https://placebear.com/300/200");
        when(client.fetch(AnimalType.BEAR)).thenReturn(fakeBear);
        when(repository.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));

        List<Picture> saved = service.fetchAndSave(AnimalType.BEAR, 3);

        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(picture -> picture.getAnimalType() == AnimalType.BEAR);
        verify(client, times(3)).fetch(AnimalType.BEAR);
    }

    @Test
    void savesNothingWhenTheProviderFails() {
        when(client.fetch(AnimalType.DOG)).thenThrow(new RestClientException("place.dog is down"));

        assertThatThrownBy(() -> service.fetchAndSave(AnimalType.DOG, 3))
                .isInstanceOf(RestClientException.class);

        verify(repository, never()).saveAll(anyList());
    }
}
