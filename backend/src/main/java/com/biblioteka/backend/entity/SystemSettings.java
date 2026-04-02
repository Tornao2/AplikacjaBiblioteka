package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettings {
    @Id
    @Column(name = "id")
    private Long id = 1L;
    @Column(name = "max_loan_duration", nullable = false)
    private Integer maxLoanDuration;
    @Column(name = "user_loan_limit", nullable = false)
    private Integer userLoanLimit;
    @Column(name = "daily_penalty_rate", nullable = false)
    private Double dailyPenaltyRate;
    public void updateFromDto(com.biblioteka.backend.dto.SystemSettingsDTO dto) {
        this.maxLoanDuration = dto.getMaxLoanDuration();
        this.userLoanLimit = dto.getUserLoanLimit();
        this.dailyPenaltyRate = dto.getDailyPenaltyRate();
    }
}