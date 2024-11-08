// AuthorDTO.java
package com.example.library.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AuthorDTO {
    private Long id;
    private String name;
    private Set<String> bookIds; // We'll just store book IDs instead of full books
}