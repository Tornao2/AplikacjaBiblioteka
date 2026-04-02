package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "staff_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary; // BigDecimal dla precyzji finansowej w Oracle
}