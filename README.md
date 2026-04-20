# — Spring Boot Bank Management System

A secure, full-stack banking web application built with Java 21, Spring Boot 3, Spring Security, and an H2/PostgreSQL database.


![tt](https://github.com/user-attachments/assets/81cbfe4f-6476-46b5-9663-74b0012d1587)
![ff](https://github.com/user-attachments/assets/f7cf41e5-5197-4235-901a-e421b0230052)

---

## Quick Start

### Requirements
- Java 21+
- Maven 3.8+

### Run
```bash
cd SKYbank
mvn spring-boot:run
```
Then open: **http://localhost:8080**

### Demo Login
| Field    | Value        |
|----------|--------------|
| Username | `john.doe`   |
| Password | `password123`|

---

## Architecture

```
src/main/java/com/MMCBank/
├── MMCBankApplication.java     ← Entry point + seed data
├── config/
│   └── SecurityConfig.java       ← Spring Security + JWT + BCrypt + CORS
├── controller/
│   ├── AuthController.java       ← POST /api/auth/login, /register
│   └── BankController.java       ← GET/POST /api/bank/**
├── dto/                          ← Request/Response records
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── TransferRequest.java
│   ├── AuthResponse.java
│   ├── AccountResponse.java
│   ├── TransactionResponse.java
│   └── UserProfileResponse.java
├── entity/
│   ├── User.java                 ← Implements UserDetails
│   ├── Account.java
│   └── Transaction.java
├── repository/
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── security/
│   ├── EncryptionConverter.java  ← AES-256/CBC JPA attribute converter
│   ├── JwtUtil.java              ← Token generation + validation
│   └── JwtAuthenticationFilter.java ← Bearer token filter
└── service/
    └── BankService.java          ← Business logic, @Transactional transfers
```

---

## Security Features

| Feature | Implementation |
|---------|---------------|
| Password hashing | BCrypt strength 12 |
| Authentication | JWT (HMAC-SHA256, 24h expiry) |
| Field encryption | AES-128/CBC — email, phone, transaction descriptions encrypted at rest |
| Stateless sessions | No server-side session (STATELESS policy) |
| CORS | Configured for localhost dev |
| Authorization | All `/api/bank/**` endpoints require valid JWT |

---

## 📡 REST API

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Returns JWT token |
| POST | `/api/auth/register` | Creates new user |

### Banking (requires `Authorization: Bearer <token>`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bank/profile` | Current user profile |
| GET | `/api/bank/accounts` | List all accounts |
| POST | `/api/bank/accounts` | Create new account |
| GET | `/api/bank/transactions` | All transactions |
| GET | `/api/bank/accounts/{id}/transactions` | Account-specific transactions |
| POST | `/api/bank/transfer` | Execute a transfer |

### Transfer Request Body
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500.00,
  "description": "Monthly savings"
}
```

---

## Database

**Default:** H2 in-memory
- H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:aurumbank`

**Complex:** MySql — uncomment the PostgreSQL config in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/name_of_database
spring.datasource.username=username
spring.datasource.password=password
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Change this secret before deploying to production!
app.jwt.secret=your_very_long_secret_key_here

# AES encryption key (must be 16, 24 or 32 chars)
app.encryption.key=YourEncryptionKey
```

---

## Frontend

Features:
- Login & registration
- Dashboard with balance overview
- Account cards with individual transaction history
- Encrypted fund transfers
- Full transaction history with filtering
- User profile with security info

---

## Tech Stack

| Layer | Technology                           |
|-------|--------------------------------------|
| Language | Java 21                              |
| Framework | Spring Boot 3.2                      |
| Security | Spring Security 6 + JWT (JJWT 0.12)  |
| Database | H2 (dev) / MySQL                     |
| ORM | Spring Data JPA + Hibernate          |
| Encryption | AES-128/CBC (JPA AttributeConverter) |
| Password | BCrypt (strength 12)                 |
| Build | Maven                                |
| Frontend | HTML5 + CSS3 + Vanilla JS            |
