package com.bookstore.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;
}

@Data
public class OrderItemRequest {
    private Long bookId;

    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
}

@Data
public class OrderResponse {
    private Long id;
    private String customerName;
    private String customerEmail;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String orderDate;
}

@Data
public class OrderItemResponse {
    private String bookTitle;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}

@Data
public class OrderStatusUpdateRequest {
    private String status;
}