package com.bookstore.api.controller;

import com.bookstore.api.dto.OrderRequest;
import com.bookstore.api.dto.OrderResponse;
import com.bookstore.api.dto.OrderStatusUpdateRequest;
import com.bookstore.api.model.Book;
import com.bookstore.api.model.Order;
import com.bookstore.api.model.OrderItem;
import com.bookstore.api.model.User;
import com.bookstore.api.repository.BookRepository;
import com.bookstore.api.repository.OrderRepository;
import com.bookstore.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public OrderController(OrderRepository orderRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(orders.map(this::convertToOrderResponse));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Order> orders = orderRepository.findByUser(user, PageRequest.of(page, size));
        return ResponseEntity.ok(orders.map(this::convertToOrderResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) &&
                            !order.getUser().getEmail().equals(auth.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    return ResponseEntity.ok(convertToOrderResponse(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (var itemRequest : orderRequest.getItems()) {
            Book book = bookRepository.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + itemRequest.getBookId()));

            if (book.getStockQuantity() < itemRequest.getQuantity()) {
                return new ResponseEntity<>("Insufficient stock for book: " + book.getTitle(),
                        HttpStatus.BAD_REQUEST);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(book.getPrice() * itemRequest.getQuantity());
            orderItems.add(orderItem);

            totalAmount += orderItem.getPrice();

            // Update stock
            book.setStockQuantity(book.getStockQuantity() - itemRequest.getQuantity());
            bookRepository.save(book);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return new ResponseEntity<>(convertToOrderResponse(savedOrder), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                             @Valid @RequestBody OrderStatusUpdateRequest statusRequest) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(Order.OrderStatus.valueOf(statusRequest.getStatus().toUpperCase()));
                    return ResponseEntity.ok(convertToOrderResponse(orderRepository.save(order)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getUser().getName());
        response.setCustomerEmail(order.getUser().getEmail());
        response.setOrderStatus(order.getStatus().name());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        response.setItems(order.getOrderItems().stream().map(item -> {
            var itemResponse = new OrderResponse.OrderItemResponse();
            itemResponse.setBookTitle(item.getBook().getTitle());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getBook().getPrice());
            itemResponse.setSubtotal(item.getPrice());
            return itemResponse;
        }).collect(Collectors.toList()));

        return response;
    }
}