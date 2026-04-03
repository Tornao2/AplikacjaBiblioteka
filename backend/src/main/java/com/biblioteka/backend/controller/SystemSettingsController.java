package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemSettingsController {
    private final SystemSettingsService settingsService;
    @GetMapping
    public ResponseEntity<SystemSettingsDTO> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SystemSettingsDTO> updateSettings(@Valid @RequestBody SystemSettingsDTO settingsDTO) {
        SystemSettingsDTO updated = settingsService.updateSettings(settingsDTO);
        return ResponseEntity.ok(updated);
    }
}