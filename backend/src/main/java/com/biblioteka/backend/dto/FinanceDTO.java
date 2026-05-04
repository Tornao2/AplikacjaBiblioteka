package com.biblioteka.backend.dto;

import com.biblioteka.backend.entity.FinanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "Data transakcji jest wymagana")
    @PastOrPresent(message = "Data transakcji nie może być z przyszłości")
    private LocalDate date;
    @NotNull(message = "Typ transakcji (INCOME/EXPENSE) jest wymagany")
    private FinanceType type;
    @NotNull(message = "Kwota jest wymagana")
    @DecimalMin(value = "0.01", message = "Kwota transakcji musi być większa od zera")
    private BigDecimal amount;
    @Size(max = 500, message = "Opis nie może przekraczać 500 znaków")
    private String description;
}