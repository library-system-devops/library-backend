package com.example.library.controller;

import com.example.library.dto.UserDTO;
import com.example.library.model.User;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public List<User> getAllMembers() {
        return userService.getAllMembers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).get().username == authentication.name")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only admins can update users
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            if (!id.equals(user.getId())) {
                return ResponseEntity.badRequest().body("ID mismatch");
            }
            User updatedUser = userService.updateUser(user);

            // Remove password from response
            updatedUser.setPassword(null);

            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public int getUserCount() {
        return userService.getUserCount();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }


}