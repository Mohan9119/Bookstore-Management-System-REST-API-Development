# Bookstore Management System REST API

A Spring Boot-based RESTful API for managing a bookstore's inventory, users, and orders.

## Features

- Book Management (CRUD operations)
- User Authentication with JWT
- Order Processing
- Role-based Access Control (Admin/Customer)
- Swagger Documentation

## Technologies

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL Database
- JWT Authentication
- Swagger/OpenAPI Documentation

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Setup

1. Clone the repository
2. Configure MySQL database in `application.properties`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Authentication
- POST `/api/auth/register` - Register a new user
- POST `/api/auth/login` - Authenticate user and get token

### Books
- GET `/api/books` - Get all books (paginated)
- GET `/api/books/{id}` - Get book by ID
- POST `/api/books` - Add new book (Admin only)
- PUT `/api/books/{id}` - Update book (Admin only)
- DELETE `/api/books/{id}` - Delete book (Admin only)

### Orders
- GET `/api/orders` - Get all orders (Admin only)
- GET `/api/orders/my-orders` - Get user's orders
- GET `/api/orders/{id}` - Get order by ID
- POST `/api/orders` - Create new order
- PUT `/api/orders/{id}/status` - Update order status (Admin only)

## Security

- JWT-based authentication
- Role-based authorization (ADMIN/CUSTOMER)
- Secured endpoints with Spring Security

## Database Schema

### Books Table
- id (Primary Key)
- title
- author
- genre
- isbn
- price
- description
- stock_quantity
- image_url

### Users Table
- id (Primary Key)
- name
- email
- password (hashed)
- role

### Orders Table
- id (Primary Key)
- user_id (Foreign Key)
- order_date
- status
- payment_status
- total_amount

### Order Items Table
- id (Primary Key)
- order_id (Foreign Key)
- book_id (Foreign Key)
- quantity
- price