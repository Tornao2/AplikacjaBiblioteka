package com.biblioteka.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<com.project.crud.backend.entity.Book, Long> {
    List<com.project.crud.backend.entity.Book> findByTitleContainingIgnoreCase(String title);
    List<com.project.crud.backend.entity.Book> findByAuthorContainingIgnoreCase(String author);
    Optional<com.project.crud.backend.entity.Book> findByIsbn(String isbn);
}