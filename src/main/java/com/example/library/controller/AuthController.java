package com.example.library.controller;

import com.example.library.dto.LoginRequestDTO;
import com.example.library.dto.LoginResponseDTO;
import com.example.library.dto.RegistrationDTO;
import com.example.library.dto.StaffRegistrationDTO;
import com.example.library.model.User;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDTO registrationDTO) {
        try {
            User user = userService.registerMember(registrationDTO);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register-staff")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('LIBRARIAN') and #registrationDTO.role == 'MEMBER')")
    public ResponseEntity<?> registerStaff(@Valid @RequestBody StaffRegistrationDTO registrationDTO) {
        try {
            // Validate role permissions
            if (registrationDTO.getRole().equals("ADMIN") &&
                    !userService.getCurrentUser().getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.status(403).body("Only administrators can create admin accounts");
            }

            if (registrationDTO.getRole().equals("LIBRARIAN") &&
                    !userService.getCurrentUser().getRole().equals(User.Role.ADMIN)) {
                return ResponseEntity.status(403).body("Only administrators can create librarian accounts");
            }

            User user = userService.registerStaff(registrationDTO);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}