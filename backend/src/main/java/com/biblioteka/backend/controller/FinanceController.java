package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.FinanceType;
import com.biblioteka.backend.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<FinanceDTO>> getAll() {
        return ResponseEntity.ok(financeService.getAllFinances());
    }

    @PostMapping
    public ResponseEntity<FinanceDTO> addEntry(@RequestBody FinanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.addEntry(dto));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Double>> getSummary() {
        List<FinanceDTO> all = financeService.getAllFinances();
        double totalIncome = all.stream()
                .filter(f -> f.getType() == FinanceType.INCOME)
                .mapToDouble(FinanceDTO::getAmount)
                .sum();

        double totalExpense = all.stream()
                .filter(f -> f.getType() == FinanceType.EXPENSE)
                .mapToDouble(FinanceDTO::getAmount)
                .sum();
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("balance", totalIncome - totalExpense);
        return ResponseEntity.ok(summary);
    }
}