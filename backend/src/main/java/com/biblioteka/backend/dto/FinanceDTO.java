package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.FinanceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDTO {
    private Long id;
    private LocalDate date;
    private FinanceType type;
    private Double amount;
    private String description;
}