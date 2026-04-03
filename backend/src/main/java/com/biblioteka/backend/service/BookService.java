package com.biblioteka.backend.service;

import com.biblioteka.backend.dto.BookDTO;
import com.biblioteka.backend.entity.Book;
import com.biblioteka.backend.repository.BookRepository;
import com.biblioteka.backend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final SystemLogService logService;

    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookDTO saveBook(BookDTO dto) {
        boolean isUpdate = dto.getId() != null;
        Book book = Book.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .category(dto.getCategory())
                .status(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE")
                .description(dto.getDescription())
                .releaseYear(dto.getReleaseYear())
                .build();
        Book saved = bookRepository.save(book);
        String action = isUpdate ? "BOOK_UPDATED" : "BOOK_ADDED";
        logService.addLog("SYSTEM", action, "Książka: " + saved.getTitle(), "INFO");
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        boolean isBorrowed = loanRepository.findByBookIdAndReturnDateIsNull(id)
                .stream().findAny().isPresent();
        if (isBorrowed) {
            throw new RuntimeException("Nie można usunąć książki, która jest obecnie wypożyczona!");
        }
        bookRepository.findById(id).ifPresent(book -> {
            bookRepository.delete(book);
            logService.addLog("SYSTEM", "BOOK_DELETED", "Usunięto: " + book.getTitle(), "WARNING");
        });
    }

    private BookDTO mapToDTO(Book entity) {
        return BookDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .isbn(entity.getIsbn())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .releaseYear(entity.getReleaseYear())
                .build();
    }
}