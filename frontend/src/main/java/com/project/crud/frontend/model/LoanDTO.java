package com.project.crud.frontend.model;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String bookTitle;
    private String bookAuthor;
    private String userFullName;
    private String userEmail;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean extended;
    private Long overduePay;
    public String getStatus() {
        if (returnDate != null) {
            return "ZWRÓCONA";
        }
        if (isOverdue()) {
            return "PO TERMINIE";
        }
        return "AKTYWNE";
    }
    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }
    public double getOverduePayFormatted() {
        return overduePay / 100.0;
    }
}