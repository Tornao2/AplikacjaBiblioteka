package com.biblioteka.backend.dto;

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
    private String type;
    private Double amount;
    private String description;
}