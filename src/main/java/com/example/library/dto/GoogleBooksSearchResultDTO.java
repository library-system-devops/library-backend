// GoogleBooksSearchResultDTO.java
package com.example.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoogleBooksSearchResultDTO {
    private Integer totalItems;
    private List<GoogleBooksDTO> items;
}