// BookDTO.java
package com.example.library.dto;

import lombok.Data;
import java.util.Set;

@Data
public class BookDTO {
    private String id;
    private String title;
    private String publishedDate;
    private String description;
    private Double averageRating;
    private Integer ratingsCount;
    private String thumbnailUrl;
    private Integer copiesOwned;
    private Integer copiesAvailable;
    private Set<String> authors;
    private Set<String> categories;
    private String policyType = "BOOK";
}