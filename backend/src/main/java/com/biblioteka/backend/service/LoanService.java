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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje w bazie."));
        List<Loan> loans = loanRepository.findAll().stream()
                .filter(loan -> loan.getUserId().equals(currentUser.getId()))
                .toList();
        Set<Long> bookIds = loans.stream().map(Loan::getBookId).collect(Collectors.toSet());
        Map<Long, Book> booksMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        return loans.stream()
                .map(loan -> mapToDTOWithCache(loan, booksMap.get(loan.getBookId()), currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanDTO> getAllLoansForAdmin() {
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje w bazie."));
        boolean isAdminOrStaff = currentUser.getRole() != null &&
                (currentUser.getRole().name().equalsIgnoreCase("Admin") ||
                        currentUser.getRole().name().equalsIgnoreCase("Bibliotekarz"));
        if (!isAdminOrStaff) {
            throw new RuntimeException("Brak uprawnień do przeglądania wszystkich wypożyczeń.");
        }
        List<Loan> loans = loanRepository.findAll();
        Set<Long> bookIds = loans.stream().map(Loan::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = loans.stream().map(Loan::getUserId).collect(Collectors.toSet());
        Map<Long, Book> booksMap = bookRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        Map<Long, User> usersMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return loans.stream()
                .map(loan -> mapToDTOWithCache(loan, booksMap.get(loan.getBookId()), usersMap.get(loan.getUserId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO prolongLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wypożyczenia o ID: " + loanId));
        User borrowingUser = userRepository.findById(loan.getUserId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika przypisanego do wypożyczenia."));
        String currentUsername = getCurrentUsername();
        if ("SYSTEM".equals(currentUsername)) {
            throw new RuntimeException("Brak autoryzacji.");
        }
        if (!borrowingUser.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Nie masz uprawnień do przedłużenia tego wypożyczenia!");
        }
        if (loan.getReturnDate() != null) {
            throw new RuntimeException("Nie można przedłużyć już zwróconego wypożyczenia.");
        }
        if (loan.isExtended()) {
            throw new RuntimeException("To wypożyczenie zostało już raz przedłużone.");
        }
        loan.setDueDate(loan.getDueDate().plusDays(7));
        loan.setExtended(true);
        Loan saved = loanRepository.save(loan);
        logService.addLog(currentUsername, "LOAN_EXTENDED",
                "Przedłużono termin wypożyczenia ID: " + saved.getId() + " dla użytkownika: " + borrowingUser.getUsername(), "INFO");
        Book book = bookRepository.findById(saved.getBookId()).orElse(null);
        return mapToDTOWithCache(saved, book, borrowingUser);
    }

    @Transactional
    public LoanDTO createLoan(Long userId, Long bookId) {
        SystemSettingsDTO settings = settingsService.getSettings();
        long activeLoansCount = loanRepository.findAll().stream()
                .filter(l -> l.getUserId().equals(userId) && l.getReturnDate() == null)
                .count();
        if (activeLoansCount >= settings.getUserLoanLimit()) {
            throw new RuntimeException("Użytkownik osiągnął już maksymalny limit wypożyczeń (" + settings.getUserLoanLimit() + ").");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Książka o ID " + bookId + " nie istnieje."));
        if (!BookStatus.Dostepna.equals(book.getStatus())) {
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
        book.setStatus(BookStatus.Wypozyczona);
        bookRepository.save(book);
        Loan saved = loanRepository.save(loan);
        String operator = getCurrentUsername();
        logService.addLog(operator, "LOAN_START",
                "Wypożyczono książkę: '" + book.getTitle() + "' dla użytkownika: " + user.getUsername(), "INFO");
        return mapToDTOWithCache(saved, book, user);
    }

    @Transactional
    public void returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Wypożyczenie nie istnieje."));
        if (loan.getReturnDate() != null) {
            throw new RuntimeException("Książka została już wcześniej zwrócona.");
        }
        Book book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new RuntimeException("Książka nie istnieje w bazie."));
        loan.setReturnDate(LocalDate.now());
        book.setStatus(BookStatus.Dostepna);
        bookRepository.save(book);
        loanRepository.save(loan);
        String operator = getCurrentUsername();
        logService.addLog(operator, "LOAN_RETURN", "Zwrócono książkę o ID: " + book.getId(), "INFO");
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    private LoanDTO mapToDTOWithCache(Loan entity, Book book, User user) {
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