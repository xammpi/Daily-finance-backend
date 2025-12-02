# Daily Finance Backend - API Documentation for Frontend

## Overview

Daily Finance Backend is a REST API for tracking daily expenses. Users can manage their balance, add expenses, categorize spending, and track monthly expenditures.

**Base URL:** `http://localhost:8080/api/v1`

**API Documentation:** `http://localhost:8080/swagger-ui.html`

## Architecture

The application follows a simplified expense tracking model:
- Users have a **balance** (current available money)
- Users can **deposit** money to their balance
- Users create **expenses** which automatically deduct from their balance
- Expenses are organized into **categories**
- System tracks **monthly expenses** and calculates remaining balance

## Authentication

### Authentication Flow

1. **Register** a new user account
2. **Login** to receive JWT tokens
3. **Include token** in all subsequent requests

### Register New User

**Endpoint:** `POST /api/v1/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe"
}
```

### Login

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe"
}
```

### Using Authentication Token

Include the token in the `Authorization` header for all protected endpoints:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example with fetch:**
```javascript
const response = await fetch('http://localhost:8080/api/v1/user/profile', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
```

---

## User Profile & Balance Management

### Get User Profile

Get current user's profile information including balance and currency.

**Endpoint:** `GET /api/v1/user/profile`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "balance": 1500.50,
  "currency": "USD"
}
```

### Deposit Money

Add money to user's balance.

**Endpoint:** `POST /api/v1/user/deposit`

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "amount": 500.00
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "balance": 2000.50,
  "currency": "USD"
}
```

**Validation:**
- `amount` must be positive number
- `amount` is required

### Update Currency

Change user's preferred currency.

**Endpoint:** `PUT /api/v1/user/currency?currency=EUR`

**Headers:** `Authorization: Bearer {token}`

**Query Parameters:**
- `currency` - Currency code (USD, EUR, GBP, etc.)

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "balance": 2000.50,
  "currency": "EUR"
}
```

**Available Currencies:**
`USD`, `EUR`, `GBP`, `JPY`, `CNY`, `RUB`, `UAH`, `PLN`, `CHF`, `CAD`, `AUD`, `BRL`, `INR`, `KRW`, `MXN`, `SEK`, `NOK`, `DKK`, `TRY`, `ZAR`

### Get Balance Summary

Get current balance with monthly expense statistics.

**Endpoint:** `GET /api/v1/user/balance-summary`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "currentBalance": 2000.50,
  "totalExpensesThisMonth": 450.75,
  "remainingBalance": 2000.50,
  "currency": "USD"
}
```

**Notes:**
- `currentBalance` - User's current available balance
- `totalExpensesThisMonth` - Sum of all expenses in the current calendar month
- `remainingBalance` - Same as currentBalance (expenses already deducted)
- Resets monthly calculation on the 1st of each month

---

## Expense Management

Expenses automatically deduct from user balance when created.

### Create Expense

**Endpoint:** `POST /api/v1/expenses`

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "amount": 45.99,
  "date": "2024-12-02",
  "description": "Grocery shopping",
  "categoryId": 1
}
```

**Response:** `201 Created`
```json
{
  "id": 15,
  "amount": 45.99,
  "date": "2024-12-02",
  "description": "Grocery shopping",
  "categoryId": 1,
  "categoryName": "Food & Dining"
}
```

**Validation:**
- `amount` must be positive
- `date` cannot be in the future
- `categoryId` must exist and belong to the user
- User balance will be reduced by the amount

**Balance Impact:**
- Before: balance = 2000.50
- After expense creation: balance = 1954.51

### Get All Expenses

**Endpoint:** `GET /api/v1/expenses`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
[
  {
    "id": 15,
    "amount": 45.99,
    "date": "2024-12-02",
    "description": "Grocery shopping",
    "categoryId": 1,
    "categoryName": "Food & Dining"
  },
  {
    "id": 14,
    "amount": 120.00,
    "date": "2024-12-01",
    "description": "Electric bill",
    "categoryId": 3,
    "categoryName": "Utilities"
  }
]
```

### Get Expense by ID

**Endpoint:** `GET /api/v1/expenses/{id}`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "id": 15,
  "amount": 45.99,
  "date": "2024-12-02",
  "description": "Grocery shopping",
  "categoryId": 1,
  "categoryName": "Food & Dining"
}
```

**Error Response:** `404 Not Found` if expense doesn't exist or doesn't belong to user

### Update Expense

**Endpoint:** `PUT /api/v1/expenses/{id}`

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "amount": 52.99,
  "date": "2024-12-02",
  "description": "Grocery shopping (updated)",
  "categoryId": 1
}
```

**Response:** `200 OK`
```json
{
  "id": 15,
  "amount": 52.99,
  "date": "2024-12-02",
  "description": "Grocery shopping (updated)",
  "categoryId": 1,
  "categoryName": "Food & Dining"
}
```

**Balance Impact:**
- Old expense amount is added back to balance
- New expense amount is deducted from balance
- Example: Old 45.99 → New 52.99 means balance decreases by 7.00

### Delete Expense

**Endpoint:** `DELETE /api/v1/expenses/{id}`

**Headers:** `Authorization: Bearer {token}`

**Response:** `204 No Content`

**Balance Impact:**
- Expense amount is added back to user's balance
- Example: Delete 45.99 expense → balance increases by 45.99

---

## Category Management

Categories help organize expenses. Each user has their own categories.

### Get All Categories

**Endpoint:** `GET /api/v1/categories`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Food & Dining",
    "description": "Groceries, restaurants, cafes"
  },
  {
    "id": 2,
    "name": "Transportation",
    "description": "Gas, public transport, parking"
  },
  {
    "id": 3,
    "name": "Utilities",
    "description": "Electric, water, internet bills"
  }
]
```

### Create Category

**Endpoint:** `POST /api/v1/categories`

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "name": "Entertainment",
  "description": "Movies, games, subscriptions"
}
```

**Response:** `201 Created`
```json
{
  "id": 4,
  "name": "Entertainment",
  "description": "Movies, games, subscriptions"
}
```

**Validation:**
- `name` is required and cannot be blank
- `description` is optional

### Get Category by ID

**Endpoint:** `GET /api/v1/categories/{id}`

**Headers:** `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Food & Dining",
  "description": "Groceries, restaurants, cafes"
}
```

### Update Category

**Endpoint:** `PUT /api/v1/categories/{id}`

**Headers:** `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "name": "Food & Dining",
  "description": "All food-related expenses"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Food & Dining",
  "description": "All food-related expenses"
}
```

### Delete Category

**Endpoint:** `DELETE /api/v1/categories/{id}`

**Headers:** `Authorization: Bearer {token}`

**Response:** `204 No Content`

**Note:** Cannot delete category if it has associated expenses. Delete or reassign expenses first.

---

## Error Handling

All error responses follow this format:

```json
{
  "timestamp": "2024-12-02T10:54:45.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Category does not belong to current user",
  "path": "/api/v1/expenses"
}
```

### Common Error Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 400 | Bad Request | Validation failed, invalid data, business rule violation |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist or doesn't belong to user |
| 500 | Internal Server Error | Server error, check logs |

### Validation Errors

When validation fails, you'll receive detailed field-level errors:

```json
{
  "timestamp": "2024-12-02T10:54:45.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/expenses",
  "details": {
    "amount": "must be greater than 0",
    "categoryId": "must not be null"
  }
}
```

---

## Frontend Integration Examples

### React/TypeScript Example

```typescript
// API Client
const API_BASE_URL = 'http://localhost:8080/api/v1';

interface LoginRequest {
  username: string;
  password: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  username: string;
}

interface ExpenseRequest {
  amount: number;
  date: string;
  description: string;
  categoryId: number;
}

interface ExpenseResponse {
  id: number;
  amount: number;
  date: string;
  description: string;
  categoryId: number;
  categoryName: string;
}

class ApiClient {
  private token: string | null = null;

  setToken(token: string) {
    this.token = token;
    localStorage.setItem('accessToken', token);
  }

  getToken(): string | null {
    return this.token || localStorage.getItem('accessToken');
  }

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });

    if (!response.ok) throw new Error('Login failed');

    const data = await response.json();
    this.setToken(data.accessToken);
    return data;
  }

  async createExpense(expense: ExpenseRequest): Promise<ExpenseResponse> {
    const response = await fetch(`${API_BASE_URL}/expenses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      },
      body: JSON.stringify(expense)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }

    return response.json();
  }

  async getBalanceSummary() {
    const response = await fetch(`${API_BASE_URL}/user/balance-summary`, {
      headers: {
        'Authorization': `Bearer ${this.getToken()}`
      }
    });

    if (!response.ok) throw new Error('Failed to fetch balance');
    return response.json();
  }
}

export const api = new ApiClient();
```

### Vue.js Example with Axios

```javascript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add token to all requests
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle authentication errors
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Redirect to login
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default {
  // Auth
  login(credentials) {
    return apiClient.post('/auth/login', credentials);
  },

  register(userData) {
    return apiClient.post('/auth/register', userData);
  },

  // Expenses
  getExpenses() {
    return apiClient.get('/expenses');
  },

  createExpense(expense) {
    return apiClient.post('/expenses', expense);
  },

  updateExpense(id, expense) {
    return apiClient.put(`/expenses/${id}`, expense);
  },

  deleteExpense(id) {
    return apiClient.delete(`/expenses/${id}`);
  },

  // User
  getProfile() {
    return apiClient.get('/user/profile');
  },

  depositMoney(amount) {
    return apiClient.post('/user/deposit', { amount });
  },

  getBalanceSummary() {
    return apiClient.get('/user/balance-summary');
  },

  // Categories
  getCategories() {
    return apiClient.get('/categories');
  },

  createCategory(category) {
    return apiClient.post('/categories', category);
  }
};
```

---

## Testing the API

### Using cURL

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"SecurePass123!"}'
```

**Create Expense:**
```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "amount": 45.99,
    "date": "2024-12-02",
    "description": "Grocery shopping",
    "categoryId": 1
  }'
```

### Using Postman

1. Import the OpenAPI spec from `http://localhost:8080/v3/api-docs`
2. Set environment variable `token` with your JWT
3. Add `Authorization: Bearer {{token}}` to collection

---

## Data Models Reference

### Currency Enum
```
USD - US Dollar ($)
EUR - Euro (€)
GBP - British Pound (£)
JPY - Japanese Yen (¥)
CNY - Chinese Yuan (¥)
RUB - Russian Ruble (₽)
UAH - Ukrainian Hryvnia (₴)
PLN - Polish Zloty (zł)
CHF - Swiss Franc (CHF)
CAD - Canadian Dollar (C$)
AUD - Australian Dollar (A$)
BRL - Brazilian Real (R$)
INR - Indian Rupee (₹)
KRW - South Korean Won (₩)
MXN - Mexican Peso ($)
SEK - Swedish Krona (kr)
NOK - Norwegian Krone (kr)
DKK - Danish Krone (kr)
TRY - Turkish Lira (₺)
ZAR - South African Rand (R)
```

---

## Best Practices

### For Frontend Developers

1. **Token Storage:**
   - Store JWT in `localStorage` or secure `httpOnly` cookie
   - Clear token on logout
   - Refresh token before expiration (24 hours)

2. **Error Handling:**
   - Always check response status codes
   - Display user-friendly error messages
   - Handle 401 errors by redirecting to login

3. **Balance Updates:**
   - Refresh balance summary after expense operations
   - Show real-time balance updates in UI
   - Warn user if balance is low

4. **Date Formatting:**
   - API expects ISO date format: `YYYY-MM-DD`
   - Convert user timezone to UTC if needed

5. **Validation:**
   - Validate on frontend before API call
   - Match backend validation rules
   - Show field-level errors from API response

6. **Performance:**
   - Cache categories list (rarely changes)
   - Implement pagination for large expense lists
   - Debounce search/filter operations

---

## Changelog

### Version 2.0.0 (December 2024)
- **BREAKING:** Removed Transaction entity, replaced with Expense
- **BREAKING:** Removed Account entity, balance moved to User
- Added user balance deposit endpoint
- Added balance summary with monthly expense calculation
- Simplified expense tracking (no INCOME/EXPENSE types)
- Added 20 currency support
- Removed messages.properties, using default validation
- Simplified Category entity (removed hierarchy)

### Version 1.0.0 (November 2024)
- Initial release with full authentication
- Transaction management with Account support
- Category hierarchy support
- JWT authentication

---

## Support & Resources

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health
- **GitHub:** [Repository URL]
- **Issues:** Report bugs via GitHub Issues

---

## Quick Start Checklist

- [ ] Backend running on http://localhost:8080
- [ ] Register a new user account
- [ ] Login and save JWT token
- [ ] Create at least one category
- [ ] Deposit initial balance
- [ ] Create your first expense
- [ ] Check balance summary
- [ ] Test expense update/delete
- [ ] Verify balance adjusts correctly

---

**Last Updated:** December 2, 2024
**API Version:** 2.0.0
**Spring Boot Version:** 3.2.5
