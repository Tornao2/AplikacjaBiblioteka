package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemLogDTO;
import com.biblioteka.backend.entity.SystemLog;
import com.biblioteka.backend.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemLogService {
    private final SystemLogRepository logRepository;

    @Transactional(readOnly = true)
    public List<SystemLogDTO> getAllLogs() {
        return logRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addLog(String user, String action, String details, String severity) {
        SystemLog log = SystemLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .severity(severity)
                .timestamp(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    private SystemLogDTO mapToDTO(SystemLog entity) {
        return SystemLogDTO.builder()
                .timestamp(entity.getTimestamp())
                .user(entity.getUser())
                .action(entity.getAction())
                .details(entity.getDetails())
                .severity(entity.getSeverity())
                .build();
    }
}