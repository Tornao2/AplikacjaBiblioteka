package com.biblioteka.backend.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    private Long id;
    private UserDTO user;
    private String phoneNumber;
    private LocalDate hireDate;
    private Double salary;
}