package com.project.crud.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {
    private Integer maxLoanDuration;
    private Integer userLoanLimit;
    private Double dailyPenaltyRate;
    private java.time.LocalDateTime lastModified;
}