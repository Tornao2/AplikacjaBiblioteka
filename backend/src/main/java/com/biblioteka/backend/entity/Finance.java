package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "finances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false, length = 50)
    private String type;
    @Column(precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(length = 500)
    private String description;
}