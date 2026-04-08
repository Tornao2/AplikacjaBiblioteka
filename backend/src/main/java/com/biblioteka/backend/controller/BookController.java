package com.biblioteka.backend.controller;

import com.biblioteka.backend.dto.BookDTO;
import com.biblioteka.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAll() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @PreAuthorize("hasAnyRole('Admin', 'Bibliotekarz')")
    @PostMapping
    public ResponseEntity<BookDTO> add(@Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.saveBook(bookDTO));
    }

    @PreAuthorize("hasAnyRole('Admin', 'Bibliotekarz')")
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> update(@PathVariable Long id, @Valid @RequestBody BookDTO bookDTO) {
        bookDTO.setId(id);
        return ResponseEntity.ok(bookService.saveBook(bookDTO));
    }

    @PreAuthorize("hasAnyRole('Admin', 'Bibliotekarz')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}