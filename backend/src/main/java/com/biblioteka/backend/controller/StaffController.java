package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.StaffDTO;
import com.biblioteka.backend.dto.StaffRegistrationRequest;
import com.biblioteka.backend.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StaffController {
    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@RequestBody StaffRegistrationRequest request) {
        try {
            StaffDTO createdStaff = staffService.createStaff(request);
            return ResponseEntity.ok(createdStaff);
        } catch (Exception e) {
            if (e.getMessage().contains("zajęty") || e.getMessage().contains("exists")) {
                return ResponseEntity.status(400).body("400:Login lub email jest już zajęty.");
            }
            return ResponseEntity.status(500).body("Błąd podczas tworzenia pracownika.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeStaff(@PathVariable Long id) {
        try {
            staffService.removeStaffKeepUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}