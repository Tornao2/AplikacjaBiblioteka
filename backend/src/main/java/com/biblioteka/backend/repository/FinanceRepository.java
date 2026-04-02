package com.biblioteka.backend.repository;

import com.biblioteka.backend.entity.Finance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceRepository extends JpaRepository<Finance, Long> {
    List<Finance> findByDateBetween(LocalDate start, LocalDate end);
    List<Finance> findByType(String type);
}