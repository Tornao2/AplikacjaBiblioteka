package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.SystemSettingsDTO;
import com.biblioteka.backend.entity.*;
import com.biblioteka.backend.repository.BookRepository;
import com.biblioteka.backend.repository.LoanRepository;
import com.biblioteka.backend.repository.UserRepository;
import com.biblioteka.backend.service.obserwator.LoanReturnedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private SystemSettingsService settingsService;
    @Mock private SystemLogService logService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication auth;

    @InjectMocks private LoanService loanService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Create: błąd gdy przekroczono limit wypożyczeń")
    void createLoanLimitExceeded() {
        Long userId = 1L;
        SystemSettingsDTO settings = new SystemSettingsDTO();
        settings.setUserLoanLimit(2);
        when(settingsService.getSettings()).thenReturn(settings);
        when(loanRepository.findAll()).thenReturn(List.of(
                Loan.builder().userId(userId).build(),
                Loan.builder().userId(userId).build()
        ));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.createLoan(userId, 10L));
        assertTrue(ex.getMessage().contains("maksymalny limit"),
                "Błędny komunikat: " + ex.getMessage());
    }

    @Test
    @DisplayName("Create: błąd gdy książka jest już wypożyczona")
    void createLoanBookBusy() {
        Long bookId = 10L;
        Long userId = 1L;
        SystemSettingsDTO settings = new SystemSettingsDTO();
        settings.setUserLoanLimit(5);
        when(settingsService.getSettings()).thenReturn(settings);
        when(loanRepository.findAll()).thenReturn(List.of());
        Book book = Book.builder()
                .id(bookId)
                .status(BookStatus.Wypozyczona)
                .build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.createLoan(userId, bookId));
        assertEquals("Książka jest już wypożyczona.", ex.getMessage());
    }

    @Test
    @DisplayName("Prolong: błąd gdy po terminie")
    void prolongAfterDueDate() {
        Loan loan = Loan.builder().id(1L).dueDate(LocalDate.now().minusDays(1)).userId(1L).build();
        User user = User.builder().id(1L).username("testUser").build();
        mockSecurity("testUser");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.prolongLoan(1L));
        assertEquals("Jest już po terminie.", ex.getMessage());
    }

    @Test
    @DisplayName("Prolong: błąd gdy już raz przedłużono")
    void prolongAlreadyExtended() {
        Loan loan = Loan.builder().id(1L).dueDate(LocalDate.now().plusDays(5)).extended(true).userId(1L).build();
        User user = User.builder().id(1L).username("testUser").build();
        mockSecurity("testUser");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.prolongLoan(1L));
        assertEquals("To wypożyczenie zostało już raz przedłużone.", ex.getMessage());
    }

    @Test
    @DisplayName("Return: Sukces i publikacja zdarzenia (Obserwator)")
    void returnBookSuccessAndPublishEvent() {
        Long loanId = 5L;
        Book book = Book.builder().id(10L).title("Java Core").status(BookStatus.Wypozyczona).build();
        Loan loan = Loan.builder().id(loanId).bookId(10L).userId(1L).overduePay(500L).build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        mockSecurity("bibliotekarz");
        loanService.returnBook(loanId);
        assertEquals(BookStatus.Dostepna, book.getStatus());
        assertNotNull(loan.getReturnDate());
        ArgumentCaptor<LoanReturnedEvent> eventCaptor = ArgumentCaptor.forClass(LoanReturnedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        LoanReturnedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(5.0, publishedEvent.penaltyAmount());
        assertEquals("Java Core", publishedEvent.bookTitle());
        assertEquals("bibliotekarz", publishedEvent.username());
    }

    @Test
    @DisplayName("Admin: błąd gdy brak uprawnień")
    void getAllLoansForAdminAccessDenied() {
        User user = User.builder().username("user").role(UserRole.Czytelnik).build();
        mockSecurity("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loanService.getAllLoansForAdmin());
        assertEquals("Brak uprawnień do przeglądania wszystkich wypożyczeń.", ex.getMessage());
    }

    private void mockSecurity(String username) {
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(username);
        when(auth.getPrincipal()).thenReturn(username);
    }
}