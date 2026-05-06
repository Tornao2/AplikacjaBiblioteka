package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.BookDTO;
import com.biblioteka.backend.entity.Book;
import com.biblioteka.backend.entity.BookStatus;
import com.biblioteka.backend.entity.Loan;
import com.biblioteka.backend.repository.BookRepository;
import com.biblioteka.backend.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock private BookRepository bookRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private SystemLogService logService;
    @Mock private Authentication auth;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }
    @Test
    @DisplayName("Zapis: błąd przy złym statusie")
    void saveInvalidStatus() {
        BookDTO dto = BookDTO.builder().status(BookStatus.Wypozyczona).build();
        assertThrows(RuntimeException.class, () -> bookService.saveBook(dto));
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Zapis: nowa książka")
    void saveNewBook() {
        BookDTO dto = createValidDto();
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> {
            Book b = i.getArgument(0);
            b.setId(1L);
            return b;
        });
        bookService.saveBook(dto);
        verify(logService).addLog(eq("SYSTEM"), eq("BOOK_ADDED"), anyString(), eq("INFO"));
    }

    @Test
    @DisplayName("Zapis: nowa książka jako zalogowany")
    void saveNewBookAsAdmin() {
        mockSecurity();
        BookDTO dto = createValidDto();
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));
        bookService.saveBook(dto);
        verify(logService).addLog(eq("admin"), eq("BOOK_ADDED"), anyString(), eq("INFO"));
    }

    @Test
    @DisplayName("Update: nieistniejąca książka")
    void updateNotFound() {
        BookDTO dto = createValidDto();
        dto.setId(99L);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookService.saveBook(dto));
    }

    @Test
    @DisplayName("Usuwanie: błąd gdy wypożyczona")
    void deleteBorrowed() {
        when(loanRepository.findByBookIdAndReturnDateIsNull(1L)).thenReturn(List.of(new Loan()));
        assertThrows(RuntimeException.class, () -> bookService.deleteBook(1L));
        verify(bookRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Usuwanie: sukces z logowaniem użytkownika")
    void deleteSuccess() {
        Book book = Book.builder().id(1L).title("Test").build();
        mockSecurity();
        when(loanRepository.findByBookIdAndReturnDateIsNull(1L)).thenReturn(List.of());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        bookService.deleteBook(1L);
        verify(bookRepository).delete(book);
        verify(logService).addLog(eq("admin"), eq("BOOK_DELETED"), anyString(), eq("WARNING"));
    }

    private BookDTO createValidDto() {
        return BookDTO.builder()
                .title("Tytuł")
                .author("Autor")
                .status(BookStatus.Dostepna)
                .build();
    }

    private void mockSecurity() {
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(securityContext);
    }
}