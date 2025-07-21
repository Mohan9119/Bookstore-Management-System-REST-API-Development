package com.bookstore.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    private String description;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be greater than 0")
    private Integer stockQuantity;

    private String imageUrl;
}

@Data
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private String isbn;
    private Double price;
    private String description;
    private Integer stockQuantity;
    private String imageUrl;
}

@Data
public class BookSearchResponse {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private Double price;
    private String imageUrl;
}