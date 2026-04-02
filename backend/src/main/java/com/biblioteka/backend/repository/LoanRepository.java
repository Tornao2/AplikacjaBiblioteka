package com.biblioteka.backend.repository;

import com.biblioteka.backend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserIdAndReturnDateIsNull(Long userId); //
    List<Loan> findByBookIdAndReturnDateIsNull(Long bookId);
}