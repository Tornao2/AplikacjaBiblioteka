package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "return_date")
    private LocalDate returnDate;
    @Column(nullable = false)
    private boolean extended = false;
    @Column(name = "overdue_pay")
    private Long overduePay = 0L;
}