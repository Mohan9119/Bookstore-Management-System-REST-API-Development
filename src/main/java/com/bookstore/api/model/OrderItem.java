package com.bookstore.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    private Double price;

    @PrePersist
    public void calculatePrice() {
        if (book != null && quantity != null) {
            this.price = book.getPrice() * quantity;
        }
    }
}