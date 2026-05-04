package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.UserDTO;
import com.biblioteka.backend.dto.UserRegistrationRequest;
import com.biblioteka.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> createUser(@RequestBody @Valid UserRegistrationRequest request) {
        try {
            UserDTO createdUser = userService.createUser(request);
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("zajęty")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }

    @PutMapping("/me/email")
    public ResponseEntity<?> updateMyEmail(@RequestBody Map<String, String> payload) {
        try {
            String newEmail = payload.get("email");
            if (newEmail == null || newEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Adres email nie może być pusty.");
            }
            userService.updateCurrentEmail(newEmail);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updateMyPassword(@RequestBody Map<String, String> payload) {
        try {
            String currentPassword = payload.get("currentPassword");
            String newPassword = payload.get("newPassword");
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Hasła nie mogą być puste.");
            }
            userService.updateCurrentPassword(currentPassword, newPassword);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount() {
        try {
            userService.deleteCurrentAccount();
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd serwera: " + e.getMessage());
        }
    }
}