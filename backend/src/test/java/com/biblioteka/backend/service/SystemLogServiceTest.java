package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemLogDTO;
import com.biblioteka.backend.entity.SystemLog;
import com.biblioteka.backend.repository.SystemLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemLogServiceTest {
    @Mock private SystemLogRepository logRepository;
    @InjectMocks private SystemLogService logService;

    @Test
    @DisplayName("Pobieranie logów w kolejności desc")
    void shouldReturnMappedLogs() {
        SystemLog log = SystemLog.builder().user("admin").action("LOGIN").build();
        when(logRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(log));
        List<SystemLogDTO> result = logService.getAllLogs();
        assertFalse(result.isEmpty());
        assertEquals("admin", result.get(0).getUser());
        verify(logRepository).findAllByOrderByTimestampDesc();
    }

    @Test
    @DisplayName("Zapisywanie nowego logu")
    void shouldSaveNewLog() {
        logService.addLog("user1", "DELETE", "Details", "INFO");
        ArgumentCaptor<SystemLog> captor = ArgumentCaptor.forClass(SystemLog.class);
        verify(logRepository).save(captor.capture());
        SystemLog saved = captor.getValue();
        assertEquals("user1", saved.getUser());
        assertEquals("DELETE", saved.getAction());
        assertNotNull(saved.getTimestamp());
    }
}