package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.SystemLogDTO;
import com.biblioteka.backend.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemLogController {
    private final SystemLogService logService;

    @GetMapping
    public ResponseEntity<List<SystemLogDTO>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
}