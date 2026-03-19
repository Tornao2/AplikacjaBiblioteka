package com.project.crud.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SystemLogDTO {
    private LocalDateTime timestamp;
    private String user;
    private String action;
    private String details;
    private String severity;
}