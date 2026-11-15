package com.example.animalpictures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

@SpringBootTest
@AutoConfigureMockMvc
class PictureApiTest {

    private static final FetchedPicture FIRST = new FetchedPicture(new byte[] {1, 1, 1}, "image/jpeg", "https://example.com/first.jpg");
    private static final FetchedPicture SECOND = new FetchedPicture(new byte[] {2, 2, 2}, "image/jpeg", "https://example.com/second.jpg");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PictureRepository repository;

    @MockitoBean
    private AnimalPictureClient client;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        when(client.fetch(any())).thenReturn(FIRST, SECOND);
    }

    @Test
    void savesPicturesAndReturnsTheLatestOne() throws Exception {
        mockMvc.perform(post("/api/pictures").param("animal", "BEAR").param("count", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].animalType").value("BEAR"));

        mockMvc.perform(get("/api/pictures/BEAR/latest"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(SECOND.data()));
    }

    @Test
    void acceptsLowercaseAnimalNames() throws Exception {
        mockMvc.perform(post("/api/pictures").param("animal", "bear"))
                .andExpect(status().isCreated());
    }

    @Test
    void returns404WhenNoPictureWasSavedYet() throws Exception {
        mockMvc.perform(get("/api/pictures/CAT/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnknownAnimals() throws Exception {
        mockMvc.perform(post("/api/pictures").param("animal", "UNICORN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsTooManyPictures() throws Exception {
        mockMvc.perform(post("/api/pictures").param("animal", "DOG").param("count", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns502WhenTheProviderIsDown() throws Exception {
        when(client.fetch(any())).thenThrow(new RestClientException("provider is down"));

        mockMvc.perform(post("/api/pictures").param("animal", "DOG"))
                .andExpect(status().isBadGateway());
    }
}

