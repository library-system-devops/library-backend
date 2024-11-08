// GoogleBooksService.java
package com.example.library.service;

import com.example.library.dto.GoogleBooksSearchResultDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleBooksService {

    private final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";
    private final RestTemplate restTemplate;

    public GoogleBooksService() {
        this.restTemplate = new RestTemplate();
    }

    public GoogleBooksSearchResultDTO searchBooks(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API_URL)
                .queryParam("q", query)
                .build()
                .toUriString();

        return restTemplate.getForObject(url, GoogleBooksSearchResultDTO.class);
    }
}