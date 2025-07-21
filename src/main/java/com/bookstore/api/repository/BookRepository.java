package com.bookstore.api.repository;

import com.bookstore.api.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
        String title,
        String author,
        Pageable pageable
    );
    
    Page<Book> findByGenre(String genre, Pageable pageable);
    
    boolean existsByIsbn(String isbn);
}