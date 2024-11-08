// UserService.java
package com.example.library.service;

import com.example.library.dto.LoginRequestDTO;
import com.example.library.dto.LoginResponseDTO;
import com.example.library.dto.RegistrationDTO;
import com.example.library.dto.StaffRegistrationDTO;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import com.example.library.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllMembers() { return userRepository.findByRole(User.Role.MEMBER); }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerMember(RegistrationDTO registrationDTO) {
        validateNewUser(registrationDTO.getUsername(), registrationDTO.getEmail());

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setRole(User.Role.MEMBER);

        return userRepository.save(user);
    }

    public User registerStaff(StaffRegistrationDTO registrationDTO) {
        validateNewUser(registrationDTO.getUsername(), registrationDTO.getEmail());

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());

        try {
            user.setRole(User.Role.valueOf(registrationDTO.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role specified");
        }

        return userRepository.save(user);
    }

    private void validateNewUser(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername()).get();

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(jwt);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setUserId(user.getId());

        return response;
    }

    @Transactional
    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setStatus(updatedUser.getStatus());

        // Only update password if it's provided and not already encoded
        String newPassword = updatedUser.getPassword();
        if (newPassword != null && !newPassword.trim().isEmpty() && !newPassword.startsWith("$2a$")) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        // If password is null or empty, keep the existing password

        // Validate username uniqueness if changed
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            if (userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public int getUserCount() {
        return (int) userRepository.count();
    }
}