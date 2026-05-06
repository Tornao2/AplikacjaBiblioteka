package com.biblioteka.backend.service;

import com.biblioteka.backend.entity.Loan;
import com.biblioteka.backend.entity.SystemSettings;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.SystemSettingsRepository;
import com.biblioteka.backend.repository.UserRepository;
import com.biblioteka.backend.service.strategia.PenaltyContext;
import com.biblioteka.backend.service.strategia.PenaltyStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PenaltyTaskService {
    private final SystemSettingsRepository systemSettingsRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final PenaltyContext penaltyContext;
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void updatePenalties() {
        Double dailyRate = systemSettingsRepository.findById(1L)
                .map(SystemSettings::getDailyPenaltyRate)
                .orElse(0.5);
        List<Loan> overdueLoans = loanRepository.findAllByReturnDateIsNullAndDueDateBefore(LocalDate.now());
        for (Loan loan : overdueLoans) {
            userRepository.findById(loan.getUserId()).ifPresent(user -> {
                PenaltyStrategy strategy = penaltyContext.getStrategy(String.valueOf(user.getRole()));
                Long currentPenalty = strategy.calculate(loan.getDueDate(), LocalDate.now(), (long) (dailyRate*100));
                loan.setOverduePay(currentPenalty);
                loanRepository.save(loan);
            });
        }
    }
}