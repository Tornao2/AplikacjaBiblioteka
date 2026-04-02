package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "log_timestamp", nullable = false)
    private LocalDateTime timestamp;
    @Column(name = "username", nullable = false)
    private String user;
    @Column(nullable = false)
    private String action;
    @Column(length = 1000)
    private String details;
    @Column(nullable = false, length = 20)
    private String severity;
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}