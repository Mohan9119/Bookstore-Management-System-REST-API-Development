package com.bookstore.api.controller;

import com.bookstore.api.dto.BookRequest;
import com.bookstore.api.dto.BookResponse;
import com.bookstore.api.dto.BookSearchResponse;
import com.bookstore.api.model.Book;
import com.bookstore.api.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<Page<BookSearchResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("title"));
        Page<Book> bookPage;

        if (genre != null && !genre.isEmpty()) {
            bookPage = bookRepository.findByGenre(genre, pageRequest);
        } else if (search != null && !search.isEmpty()) {
            bookPage = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                search, search, pageRequest);
        } else {
            bookPage = bookRepository.findAll(pageRequest);
        }

        Page<BookSearchResponse> response = bookPage.map(book -> {
            BookSearchResponse dto = new BookSearchResponse();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setGenre(book.getGenre());
            dto.setPrice(book.getPrice());
            dto.setImageUrl(book.getImageUrl());
            return dto;
        });

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    BookResponse response = new BookResponse();
                    response.setId(book.getId());
                    response.setTitle(book.getTitle());
                    response.setAuthor(book.getAuthor());
                    response.setGenre(book.getGenre());
                    response.setIsbn(book.getIsbn());
                    response.setPrice(book.getPrice());
                    response.setDescription(book.getDescription());
                    response.setStockQuantity(book.getStockQuantity());
                    response.setImageUrl(book.getImageUrl());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBook(@Valid @RequestBody BookRequest bookRequest) {
        if (bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            return new ResponseEntity<>("ISBN already exists", HttpStatus.BAD_REQUEST);
        }

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setGenre(bookRequest.getGenre());
        book.setIsbn(bookRequest.getIsbn());
        book.setPrice(bookRequest.getPrice());
        book.setDescription(bookRequest.getDescription());
        book.setStockQuantity(bookRequest.getStockQuantity());
        book.setImageUrl(bookRequest.getImageUrl());

        Book savedBook = bookRepository.save(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(bookRequest.getTitle());
                    book.setAuthor(bookRequest.getAuthor());
                    book.setGenre(bookRequest.getGenre());
                    book.setPrice(bookRequest.getPrice());
                    book.setDescription(bookRequest.getDescription());
                    book.setStockQuantity(bookRequest.getStockQuantity());
                    book.setImageUrl(bookRequest.getImageUrl());
                    return new ResponseEntity<>(bookRepository.save(book), HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    bookRepository.delete(book);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}