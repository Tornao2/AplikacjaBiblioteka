package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.LoanDTO;
import com.biblioteka.backend.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {
    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @PostMapping
    public ResponseEntity<?> createLoan(@RequestParam Long userId, @RequestParam Long bookId) {
        try {
            LoanDTO loan = loanService.createLoan(userId, bookId);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("400:" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd wewnętrzny serwera.");
        }
    }
}