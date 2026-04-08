package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.LoanDTO;
import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.entity.Book;
import com.biblioteka.backend.entity.BookStatus;
import com.biblioteka.backend.entity.Loan;
import com.biblioteka.backend.entity.User;
import com.biblioteka.backend.repository.BookRepository;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SystemSettingsService settingsService;
    private final SystemLogService logService;

    @Transactional(readOnly = true)
    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO createLoan(Long userId, Long bookId) {
        SystemSettingsDTO settings = settingsService.getSettings();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Książka o ID " + bookId + " nie istnieje."));
        if (!BookStatus.AVAILABLE.equals(book.getStatus())) {
            throw new RuntimeException("Książka jest już wypożyczona.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Użytkownik o ID " + userId + " nie istnieje."));
        LocalDate now = LocalDate.now();
        Loan loan = Loan.builder()
                .userId(userId)
                .bookId(bookId)
                .loanDate(now)
                .dueDate(now.plusDays(settings.getMaxLoanDuration()))
                .extended(false)
                .overduePay(0L)
                .build();
        book.setStatus(BookStatus.RENTED);
        bookRepository.save(book);
        Loan saved = loanRepository.save(loan);
        logService.addLog(user.getUsername(), "LOAN_START",
                "Wypożyczono: " + book.getTitle(), "INFO");
        return mapToDTO(saved);
    }

    private LoanDTO mapToDTO(Loan entity) {
        Book book = bookRepository.findById(entity.getBookId()).orElse(null);
        User user = userRepository.findById(entity.getUserId()).orElse(null);
        return LoanDTO.builder()
                .id(entity.getId())
                .bookId(entity.getBookId())
                .userId(entity.getUserId())
                .bookTitle(book != null ? book.getTitle() : "Nieznany tytuł")
                .bookAuthor(book != null ? book.getAuthor() : "Nieznany autor")
                .userFullName(user != null ? user.getFullName() : "Nieznany użytkownik")
                .userEmail(user != null ? user.getEmail() : "Brak e-mail")
                .loanDate(entity.getLoanDate())
                .dueDate(entity.getDueDate())
                .returnDate(entity.getReturnDate())
                .extended(entity.isExtended())
                .overduePay(entity.getOverduePay())
                .build();
    }
}