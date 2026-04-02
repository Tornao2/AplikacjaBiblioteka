package com.biblioteka.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDTO {
    private LocalDateTime timestamp;
    private String user;
    private String action;
    private String details;
    private String severity;
}