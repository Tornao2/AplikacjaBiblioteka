package com.biblioteka.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {
    @Min(value = 1, message = "Minimalny czas wypożyczenia to 1 dzień")
    @Max(value = 365, message = "Maksymalny czas wypożyczenia to 365 dni")
    private Integer maxLoanDuration;
    @Min(value = 1, message = "Użytkownik musi móc wypożyczyć co najmniej 1 książkę")
    @Max(value = 100, message = "Maksymalny limit to 100 książek")
    private Integer userLoanLimit;
    @DecimalMin(value = "0.00", message = "Stawka kary nie może być ujemna")
    @DecimalMax(value = "100.00", message = "Stawka kary jest zbyt wysoka")
    @Digits(integer = 3, fraction = 2, message = "Stawka kary musi mieć format max 0.0x")
    private Double dailyPenaltyRate;
}