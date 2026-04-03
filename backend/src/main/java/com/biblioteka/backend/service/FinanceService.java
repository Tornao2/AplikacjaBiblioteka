package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.FinanceDTO;
import com.biblioteka.backend.entity.Finance;
import com.biblioteka.backend.repository.FinanceRepository;
import lombok.RequiredArgsConstructor;
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
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FinanceDTO addEntry(FinanceDTO dto) {
        Finance finance = Finance.builder()
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .type(dto.getType())
                .amount(BigDecimal.valueOf(dto.getAmount()))
                .description(dto.getDescription())
                .build();
        Finance saved = financeRepository.save(finance);
        logService.addLog("SYSTEM", "FINANCE_ENTRY",
                "Dodano wpis: " + dto.getType() + " na kwotę: " + dto.getAmount(), "INFO");
        return mapToDTO(saved);
    }

    private FinanceDTO mapToDTO(Finance entity) {
        return FinanceDTO.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .type(entity.getType())
                .amount(entity.getAmount().doubleValue())
                .description(entity.getDescription())
                .build();
    }
}