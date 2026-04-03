package com.project.crud.frontend.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StaffRegistrationRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private Double salary;
}