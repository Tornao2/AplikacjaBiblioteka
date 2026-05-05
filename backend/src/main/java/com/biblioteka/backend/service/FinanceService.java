package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.Finance;
import com.biblioteka.backend.repository.FinanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {
    private final FinanceRepository financeRepository;
    private final SystemLogService logService;

    @Transactional(readOnly = true)
    public List<FinanceDTO> getAllFinances() {
        return financeRepository.findAll()
                .stream()
                .sorted((f1, f2) -> f2.getDate().compareTo(f1.getDate())) // Od najnowszych
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FinanceDTO addEntry(FinanceDTO dto) {
        if (dto.getType() == null) {
            throw new RuntimeException("Typ transakcji (INCOME/EXPENSE) jest wymagany.");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Kwota transakcji musi być większa od zera.");
        }
        if (dto.getDate() != null && dto.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Data transakcji nie może być z przyszłości.");
        }
        Finance finance = Finance.builder()
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .type(dto.getType())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();
        Finance saved = financeRepository.save(finance);
        String operator = getCurrentUsername();
        logService.addLog(operator, "FINANCE_ENTRY",
                "Dodano wpis finansowy (" + saved.getType() + ") na kwotę: " + saved.getAmount() + " zł", "INFO");
        return mapToDTO(saved);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private FinanceDTO mapToDTO(Finance entity) {
        return FinanceDTO.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .type(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .build();
    }

    @Transactional
    public FinanceDTO updateEntry(Long id, FinanceDTO dto) {
        Finance finance = financeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wpisu finansowego o ID: " + id));
        if (dto.getType() == null) {
            throw new RuntimeException("Typ transakcji jest wymagany.");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Kwota transakcji musi być większa od zera.");
        }
        finance.setType(dto.getType());
        finance.setAmount(dto.getAmount());
        finance.setDescription(dto.getDescription());
        if (dto.getDate() != null) {
            finance.setDate(dto.getDate());
        }
        Finance updated = financeRepository.save(finance);
        String operator = getCurrentUsername();
        logService.addLog(operator, "FINANCE_UPDATE",
                "Zaktualizowano wpis finansowy ID: " + id + " na kwotę: " + updated.getAmount(), "INFO");
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteEntry(Long id) {
        Finance finance = financeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wpisu finansowego o ID: " + id));
        financeRepository.delete(finance);
        String operator = getCurrentUsername();
        logService.addLog(operator, "FINANCE_DELETE",
                "Usunięto wpis finansowy ID: " + id + " (" + finance.getDescription() + ")", "INFO");
    }
}