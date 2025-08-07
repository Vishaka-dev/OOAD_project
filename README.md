
# 🎁 Teddy Bear & Romantic Gift Selling Web App – Backend (Spring Boot)

A Spring Boot REST API for an e-commerce platform specializing in graduation-themed teddy bears and romantic gifts.

---

## ✅ Project Overview

Build a backend service for a gift shop web application that supports product management, customer orders, authentication, payment processing, and more.

---

## ✅ Tech Stack

- **Backend**: Java Spring Boot
- **Database**: MySQL
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: Swagger (springdoc-openapi)
- **Payment Integration**: Stripe / PayPal / Sampath Vishwa (local gateway)
- **Deployment**: Heroku

---

## ✅ Package Structure

```
com.teddybearshop
├── config               # Security, Swagger configs
├── controller           # REST controllers
├── dto                 # Data Transfer Objects
├── entity              # JPA entities
├── exception           # Global exception handling
├── repository          # JpaRepository interfaces
├── security            # JWT, filters, user details
├── service             # Business logic services
└── util                # Utility classes (e.g. Email, PDF)
```

---

## ✅ Database Design

### User
- id, name, email, password, role, addressList, orderList

### Product
- id, name, description, price, imageUrl, tags, category, isCustomizable

### Category
- id, name, description

### Order
- id, user, items, status, totalPrice, createdAt, paymentStatus, deliveryDate, message, giftWrap

### OrderItem
- id, product, quantity, price

### Review
- id, product, user, rating, comment, createdAt

---

## ✅ Features

### 🔐 Authentication (JWT-based)
- Register & Login
- Role-based access: Admin, Customer
- Secure endpoints with annotations

### 🛒 Products
- Admin: Add / Edit / Delete
- Users: View, Search, Filter by category/tags

### 📦 Orders
- Place Order (with custom message, gift wrap)
- View order history, cancel/return
- Admin dashboard for order management

### 💳 Payments
- Integrate with Stripe or PayPal
- Secure checkout and mark order as paid

### 🎁 Custom Features
- Gift wrapping
- Custom messages (text/audio/video)
- Delivery scheduling

---

## ✅ API Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST   | `/api/auth/register` | Guest | Register new user |
| POST   | `/api/auth/login` | Guest | Login, return JWT |
| GET    | `/api/products` | All | View all products |
| POST   | `/api/products` | Admin | Add product |
| PUT    | `/api/products/{id}` | Admin | Edit product |
| DELETE | `/api/products/{id}` | Admin | Delete product |
| GET    | `/api/orders/user` | User | View own orders |
| POST   | `/api/orders` | User | Place order |
| PUT    | `/api/orders/cancel/{id}` | User | Cancel order |
| GET    | `/api/orders` | Admin | View all orders |
| POST   | `/api/reviews` | User | Add review |

---

## ✅ Swagger API Documentation

- Access API docs at: `http://localhost:8080/swagger-ui.html`
- Uses springdoc-openapi

---

## ✅ Deployment Notes

- Backend: Deploy on **Heroku**
  - Configure environment variables for MySQL, JWT secret
- Frontend (React): Deploy on **Vercel**
- Enable CORS between frontend/backend

---

## ✅ Next Steps

1. Create ERD and tables in MySQL
2. Implement entities and DTOs
3. Build RESTful APIs for products, users, orders
4. Add authentication and security filters
5. Integrate Stripe/PayPal for payments
6. Deploy and test

---

## 🤝 Contribution

Feel free to fork and contribute to enhance the project!
