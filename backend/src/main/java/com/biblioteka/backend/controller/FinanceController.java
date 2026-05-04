package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.FinanceType;
import com.biblioteka.backend.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<FinanceDTO>> getAll() {
        return ResponseEntity.ok(financeService.getAllFinances());
    }

    @PostMapping
    public ResponseEntity<?> addEntry(@Valid @RequestBody FinanceDTO dto) {
        try {
            FinanceDTO saved = financeService.addEntry(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary() {
        List<FinanceDTO> all = financeService.getAllFinances();
        BigDecimal totalIncome = all.stream()
                .filter(f -> f.getType() == FinanceType.INCOME)
                .map(FinanceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = all.stream()
                .filter(f -> f.getType() == FinanceType.EXPENSE)
                .map(FinanceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("balance", balance);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @Valid @RequestBody FinanceDTO dto) {
        try {
            FinanceDTO updated = financeService.updateEntry(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        try {
            financeService.deleteEntry(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Błąd serwera: " + e.getMessage());
        }
    }
}