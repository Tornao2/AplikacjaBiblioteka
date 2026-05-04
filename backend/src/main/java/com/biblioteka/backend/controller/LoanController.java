package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.LoanDTO;
import com.biblioteka.backend.service.LoanService;
import lombok.Data;
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

    @GetMapping("/admin")
    public ResponseEntity<?> getAllLoansForAdmin() {
        try {
            return ResponseEntity.ok(loanService.getAllLoansForAdmin());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createLoan(@RequestBody LoanRequest request) {
        try {
            LoanDTO loan = loanService.createLoan(request.getUserId(), request.getBookId());
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd wewnętrzny serwera: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/prolong")
    public ResponseEntity<?> prolongLoan(@PathVariable Long id) {
        try {
            LoanDTO loan = loanService.prolongLoan(id);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            loanService.returnBook(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }

    @Data
    public static class LoanRequest {
        private Long userId;
        private Long bookId;
    }
}