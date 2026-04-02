package com.biblioteka.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 150)
    private String author;
    @Column(nullable = false, length = 20)
    private String isbn;
    @Column(nullable = false, length = 50)
    private String category;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;
    @Column(length = 2000)
    private String description;
}