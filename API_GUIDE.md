# Daily Finance Backend - API Guide

**Version:** 1.0.0
**Base URL:** `http://localhost:8080/api/v1`
**Authentication:** JWT Bearer Token

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Authentication](#authentication)
3. [Core Endpoints](#core-endpoints)
4. [Search & Filter System](#search--filter-system)
5. [Response Formats](#response-formats)
6. [Error Handling](#error-handling)
7. [Examples](#examples)

---

## Quick Start

### 1. Register a User

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "currencyId": 1
}
```

### 2. Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "username": "john_doe"
}
```

### 3. Use Token in Requests

```http
GET /api/v1/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## Authentication

### Register

**POST** `/api/v1/auth/register`

**Request:**

```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "currencyId": 1
}
```

**Response:** `201 Created`

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "userId": 1,
  "username": "john_doe"
}
```

### Login

**POST** `/api/v1/auth/login`

**Request:**

```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:** `200 OK` (same as register)

---

## Core Endpoints

### Currencies

#### Get All Currencies

**GET** `/api/v1/currencies`
**Auth:** Not required

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  },
  {
    "id": 2,
    "code": "EUR",
    "name": "Euro",
    "symbol": "€"
  }
]
```

---

### User & Wallet

#### Get User Profile

**GET** `/api/v1/user/profile`

**Response:** `200 OK`

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "currencyId": 1
}
```

#### Get Wallet Details

**GET** `/api/v1/user/wallet`

**Response:** `200 OK`

```json
{
  "id": 1,
  "amount": 5000.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  },
  "totalDeposits": 10,
  "totalDepositAmount": 10000.00,
  "totalExpenses": 50,
  "totalExpenseAmount": 5000.00,
  "lastTransactionDate": "2024-12-06T10:30:00",
  "lowBalanceWarning": false
}
```

#### Deposit Money

**POST** `/api/v1/user/deposit`

**Request:**

```json
{
  "amount": 1000.00
}
```

#### Withdraw Money

**POST** `/api/v1/user/withdraw`

**Request:**

```json
{
  "amount": 500.00
}
```

#### Update Balance

**PUT** `/api/v1/user/balance`

**Request:**

```json
{
  "amount": 5000.00
}
```

#### Update Currency

**PUT** `/api/v1/user/currency`

**Request:**

```json
{
  "currencyId": 2
}
```

---

### Categories

#### Create Category

**POST** `/api/v1/categories`

**Request:**

```json
{
  "name": "Food",
  "description": "Food and groceries"
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "name": "Food",
  "description": "Food and groceries",
  "createdAt": "2024-12-06T10:00:00",
  "updatedAt": "2024-12-06T10:00:00"
}
```

#### Get Category by ID

**GET** `/api/v1/categories/{id}`

#### Update Category

**PUT** `/api/v1/categories/{id}`

**Request:**

```json
{
  "name": "Food & Dining",
  "description": "Restaurants and groceries"
}
```

#### Delete Category

**DELETE** `/api/v1/categories/{id}`

**Response:** `204 No Content`

---

### Expenses

#### Create Expense

**POST** `/api/v1/expenses`

**Request:**

```json
{
  "amount": 50.00,
  "date": "2024-12-06",
  "description": "Lunch at restaurant",
  "categoryId": 1
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "amount": 50.00,
  "date": "2024-12-06",
  "description": "Lunch at restaurant",
  "categoryId": 1,
  "createdAt": "2024-12-06T12:00:00",
  "updatedAt": "2024-12-06T12:00:00"
}
```

#### Get Expense by ID

**GET** `/api/v1/expenses/{id}`

#### Update Expense

**PUT** `/api/v1/expenses/{id}`

#### Delete Expense

**DELETE** `/api/v1/expenses/{id}`

#### Get Expense Statistics

**GET** `/api/v1/expenses/statistics`

**Response:** `200 OK`

```json
{
  "todayExpenses": 150.00,
  "weekExpenses": 800.00,
  "monthExpenses": 2500.00,
  "totalExpenses": 10000.00,
  "averageDailyExpenses": 75.50,
  "averageWeeklyExpenses": 528.50,
  "averageMonthlyExpenses": 2100.00,
  "previousWeekExpenses": 750.00,
  "previousMonthExpenses": 2300.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  }
}
```

#### Get Category Statistics

**GET** `/api/v1/expenses/statistics/by-category?startDate=2024-12-01&endDate=2024-12-31`

**Response:** `200 OK`

```json
{
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "totalExpenses": 2500.00,
  "categoryBreakdown": [
    {
      "categoryId": 1,
      "categoryName": "Food",
      "totalAmount": 1200.00,
      "expenseCount": 30,
      "percentage": 48.00
    },
    {
      "categoryId": 2,
      "categoryName": "Transport",
      "totalAmount": 500.00,
      "expenseCount": 15,
      "percentage": 20.00
    }
  ],
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  }
}
```

---

## Search & Filter System

### Overview

The API provides a **powerful generic search system** that works with both **Expenses** and **Categories**. It supports:

- ✅ 14 search operations (EQUALS, LIKE, GREATER_THAN, BETWEEN, etc.)
- ✅ Multiple criteria (combined with AND logic)
- ✅ Nested field search (e.g., `category.name`)
- ✅ Pagination
- ✅ Sorting (any field, ASC/DESC)

### Endpoints

- **POST** `/api/v1/expenses/search` - Search expenses
- **POST** `/api/v1/categories/search` - Search categories

### Request Format

```json
{
  "criteria": [
    {
      "field": "amount",
      "operation": "GREATER_THAN",
      "value": "100"
    },
    {
      "field": "date",
      "operation": "BETWEEN",
      "value": "2024-12-01",
      "valueTo": "2024-12-31"
    },
    {
      "field": "category.name",
      "operation": "LIKE",
      "value": "Food"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "date",
  "sortOrder": "DESC"
}
```

### Search Operations

| Operation | Description | Example |
|-----------|-------------|---------|
| `EQUALS` | Exact match | `"value": "Food"` |
| `NOT_EQUALS` | Not equal | `"value": "100"` |
| `LIKE` | Contains (case-insensitive) | `"value": "food"` → matches "Food & Dining" |
| `STARTS_WITH` | Starts with | `"value": "Trans"` → matches "Transport" |
| `ENDS_WITH` | Ends with | `"value": "ing"` → matches "Dining" |
| `GREATER_THAN` | Greater than | `"value": "100"` |
| `GREATER_THAN_OR_EQUAL` | Greater or equal | `"value": "50"` |
| `LESS_THAN` | Less than | `"value": "1000"` |
| `LESS_THAN_OR_EQUAL` | Less or equal | `"value": "500"` |
| `BETWEEN` | Range | `"value": "100", "valueTo": "500"` |
| `IN` | In list | `"value": "Food,Transport,Entertainment"` |
| `NOT_IN` | Not in list | `"value": "Food,Transport"` |
| `IS_NULL` | Is null | No value needed |
| `IS_NOT_NULL` | Is not null | No value needed |

### Response Format

**Paginated Response:**

```json
{
  "content": [
    {
      "id": 1,
      "amount": 150.00,
      "date": "2024-12-06",
      "description": "Dinner",
      "categoryId": 1
    }
  ],
  "currentPage": 0,
  "pageSize": 20,
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Examples

#### Search Expenses > $100 in December

```http
POST /api/v1/expenses/search
Content-Type: application/json
```

```json
{
  "criteria": [
    {
      "field": "amount",
      "operation": "GREATER_THAN",
      "value": "100"
    },
    {
      "field": "date",
      "operation": "BETWEEN",
      "value": "2024-12-01",
      "valueTo": "2024-12-31"
    }
  ],
  "page": 0,
  "size": 10,
  "sortBy": "amount",
  "sortOrder": "DESC"
}
```

#### Search by Category Name

```http
POST /api/v1/expenses/search
Content-Type: application/json
```

```json
{
  "criteria": [
    {
      "field": "category.name",
      "operation": "LIKE",
      "value": "Food"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "date",
  "sortOrder": "DESC"
}
```

#### Search Categories with Name Starting with "F"

```http
POST /api/v1/categories/search
Content-Type: application/json
```

```json
{
  "criteria": [
    {
      "field": "name",
      "operation": "STARTS_WITH",
      "value": "F"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "name",
  "sortOrder": "ASC"
}
```

#### Get All Expenses (Empty Criteria)

```http
POST /api/v1/expenses/search
Content-Type: application/json
```

```json
{
  "criteria": [],
  "page": 0,
  "size": 20,
  "sortBy": "date",
  "sortOrder": "DESC"
}
```

---

## Error Handling

### Error Response Format

```json
{
  "timestamp": "2024-12-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Expense amount must be greater than zero",
  "path": "/api/v1/expenses"
}
```

### Common HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation error, negative amount |
| 401 | Unauthorized | Missing/invalid token |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Username/email already exists |
| 500 | Internal Server Error | Server error |

### Validation Errors

**Request:**

```http
POST /api/v1/expenses
Content-Type: application/json
```

```json
{
  "amount": -50.00,
  "categoryId": 999
}
```

**Response:** `400 Bad Request`

```json
{
  "timestamp": "2024-12-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Expense amount must be greater than zero",
  "path": "/api/v1/expenses"
}
```

---

## Complete Endpoint Reference

### Authentication (Public)
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login

### Currencies (Public)
- `GET /api/v1/currencies` - Get all currencies

### User & Wallet (Authenticated)
- `GET /api/v1/user/profile` - Get profile
- `GET /api/v1/user/wallet` - Get wallet details
- `GET /api/v1/user/balance-summary` - Get balance summary
- `POST /api/v1/user/deposit` - Deposit money
- `POST /api/v1/user/withdraw` - Withdraw money
- `PUT /api/v1/user/balance` - Update balance
- `PUT /api/v1/user/currency` - Update currency

### Categories (Authenticated)
- `GET /api/v1/categories/{id}` - Get by ID
- `POST /api/v1/categories` - Create
- `PUT /api/v1/categories/{id}` - Update
- `DELETE /api/v1/categories/{id}` - Delete
- `POST /api/v1/categories/search` - Advanced search

### Expenses (Authenticated)
- `GET /api/v1/expenses/{id}` - Get by ID
- `POST /api/v1/expenses` - Create
- `PUT /api/v1/expenses/{id}` - Update
- `DELETE /api/v1/expenses/{id}` - Delete
- `GET /api/v1/expenses/statistics` - Get statistics
- `GET /api/v1/expenses/statistics/by-category` - Category stats
- `POST /api/v1/expenses/search` - Advanced search

---

## Swagger Documentation

Interactive API documentation available at:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

---

## Notes

- All amounts are `BigDecimal` with 2 decimal places
- All dates are in `YYYY-MM-DD` format
- All datetimes are in ISO 8601 format
- Page numbers are 0-indexed
- Default page size is 20 items
- Maximum page size is 100 items
- JWT tokens expire after 24 hours
- All search operations are case-insensitive (except EQUALS)
- Multiple criteria are combined with AND logic
