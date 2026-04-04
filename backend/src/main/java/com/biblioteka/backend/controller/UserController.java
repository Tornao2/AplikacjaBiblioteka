package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.UserDTO;
import com.biblioteka.backend.dto.UserRegistrationRequest;
import com.biblioteka.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRegistrationRequest request) {
        try {
            UserDTO createdUser = userService.createUser(request);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("zajęty")) {
                return ResponseEntity.status(400).body(e.getMessage());
            }
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}