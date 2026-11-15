package com.example.animalpictures;

public record FetchedPicture(byte[] data, String contentType, String sourceUrl) {
}
