// GoogleBooksDTO.java
package com.example.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoogleBooksDTO {
    private String id;
    private VolumeInfo volumeInfo;

    @Data
    public static class VolumeInfo {
        private String title;
        private String publishedDate;
        private String description;
        private Double averageRating;
        private Integer ratingsCount;
        private ImageLinks imageLinks;
        private List<IndustryIdentifier> industryIdentifiers;
        private List<String> authors;
        private List<String> categories;
    }

    @Data
    public static class ImageLinks {
        private String thumbnail;
    }

    @Data
    public static class IndustryIdentifier {
        private String type;
        private String identifier;
    }
}