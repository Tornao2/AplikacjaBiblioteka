package com.biblioteka.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinanceTaskService {
    private final JdbcTemplate jdbcTemplate;
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void processMonthlySalaries() {
        String sumSql = "SELECT SUM(salary) FROM staff_details";
        BigDecimal totalSalaries = jdbcTemplate.queryForObject(sumSql, BigDecimal.class);
        if (totalSalaries != null && totalSalaries.compareTo(BigDecimal.ZERO) > 0) {
            String insertSql = "INSERT INTO finance (transaction_date, type, amount, description) " +
                    "VALUES (CURRENT_DATE, 'EXPENSE', ?, ?)";
            String description = "Automatyczne księgowanie wynagrodzeń pracowników";
            jdbcTemplate.update(insertSql, totalSalaries, description);
        }
    }
}