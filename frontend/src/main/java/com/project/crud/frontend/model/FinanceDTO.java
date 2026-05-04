package com.project.crud.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDTO {
    private Long id;
    private LocalDate date;
    private FinanceType type;
    private BigDecimal amount;
    private String description;
}