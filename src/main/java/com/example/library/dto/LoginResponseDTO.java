package com.example.library.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String username;
    private String role;
    private Long userId;
}