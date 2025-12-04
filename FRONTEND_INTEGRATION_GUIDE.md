# Frontend Integration Guide - Daily Finance API

> Complete guide for frontend developers integrating with Daily Finance Backend API

## Table of Contents
- [Quick Start](#quick-start)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Data Models](#data-models)
- [Common Use Cases](#common-use-cases)
- [Error Handling](#error-handling)
- [Best Practices](#best-practices)

---

## Quick Start

**Base URL:** `http://localhost:8080/api/v1`

**Swagger Documentation:** `http://localhost:8080/swagger-ui.html`

**Authentication:** All endpoints except `/auth/*` require JWT Bearer token

### Basic Request Example

```javascript
const BASE_URL = 'http://localhost:8080/api/v1';

// Authenticated request
const response = await fetch(`${BASE_URL}/user/wallet`, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## Authentication

### 1. Register New User

```javascript
POST /api/v1/auth/register

// Request
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

// Response (201 Created)
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe"
}
```

### 2. Login

```javascript
POST /api/v1/auth/login

// Request
{
  "username": "johndoe",
  "password": "SecurePass123!"
}

// Response (200 OK)
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "johndoe"
}
```

### 3. Store and Use Token

```javascript
// After login/register
localStorage.setItem('accessToken', response.accessToken);
localStorage.setItem('userId', response.userId);

// For all subsequent requests
const token = localStorage.getItem('accessToken');
fetch(url, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

## API Endpoints

### 👤 User & Wallet Management

#### Get User Profile
```javascript
GET /api/v1/user/profile

// Response
{
  "id": 1,
  "username": "johndoe",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "currencyId": 1
}
```

#### Get Wallet Details
```javascript
GET /api/v1/user/wallet

// Response
{
  "walletId": 1,
  "currentBalance": 5000.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  },
  "totalDeposits": 5,              // Number of deposits
  "totalDepositAmount": 5500.00,   // Total deposited
  "totalExpenses": 12,              // Number of expenses
  "totalExpenseAmount": 500.00,     // Total spent
  "lastTransactionDate": "2024-12-04T10:30:00",
  "lowBalanceWarning": false        // true if balance < 100
}
```

#### Deposit Money
```javascript
POST /api/v1/user/deposit

// Request
{
  "amount": 1000.00
}

// Response: UserProfileResponse
```

#### Withdraw Money
```javascript
POST /api/v1/user/withdraw

// Request
{
  "amount": 100.00,
  "description": "ATM withdrawal"  // optional
}

// Response: UserProfileResponse
// Error 400: "Insufficient balance. Current balance: 50.00"
```

#### Update Balance Directly
```javascript
PUT /api/v1/user/balance

// Request
{
  "amount": 5000.00
}

// Response: UserProfileResponse
```

#### Get Balance Summary
```javascript
GET /api/v1/user/balance-summary

// Response
{
  "currentBalance": 4500.00,
  "todayExpenses": 50.00,
  "weekExpenses": 250.00,
  "monthExpenses": 500.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  }
}
```

---

### 💰 Expense Management

#### Get All Expenses
```javascript
GET /api/v1/expenses

// Response
[
  {
    "id": 1,
    "amount": 50.00,
    "date": "2024-12-04",
    "description": "Grocery shopping",
    "categoryId": 3
  },
  ...
]
```

#### Get Single Expense
```javascript
GET /api/v1/expenses/{id}

// Response: ExpenseResponse
```

#### Create Expense
```javascript
POST /api/v1/expenses

// Request
{
  "amount": 50.00,
  "date": "2024-12-04",
  "description": "Lunch at restaurant",
  "categoryId": 2
}

// Response (201 Created): ExpenseResponse
// Error 400: "Insufficient balance. Current balance: 30.00, Required: 50.00"
```

⚠️ **Important:** Expense creation automatically deducts from wallet balance and validates sufficient funds.

#### Update Expense
```javascript
PUT /api/v1/expenses/{id}

// Request
{
  "amount": 75.00,
  "date": "2024-12-04",
  "description": "Updated description",
  "categoryId": 2
}

// Response: ExpenseResponse
```

⚠️ **Important:** Updates adjust wallet balance (adds old amount, deducts new amount).

#### Delete Expense
```javascript
DELETE /api/v1/expenses/{id}

// Response: 204 No Content
```

⚠️ **Important:** Deleting an expense adds the amount back to wallet balance.

---

### 📊 Statistics & Analytics

#### Get Expense Statistics
```javascript
GET /api/v1/expenses/statistics

// Response
{
  "todayExpenses": 50.00,
  "weekExpenses": 250.00,           // Monday-Sunday
  "monthExpenses": 500.00,
  "totalExpenses": 2500.00,         // All time
  "averageDailyExpenses": 25.50,
  "averageWeeklyExpenses": 178.50,
  "averageMonthlyExpenses": 625.00,
  "previousWeekExpenses": 300.00,   // For comparison
  "previousMonthExpenses": 550.00,  // For comparison
  "currency": { ... }
}
```

**Use cases:**
- Dashboard overview
- Spending trends
- Week-over-week comparison
- Month-over-month comparison

#### Get Category Statistics
```javascript
GET /api/v1/expenses/statistics/by-category?startDate=2024-12-01&endDate=2024-12-31

// Query params (optional):
// - startDate: "YYYY-MM-DD" (defaults to current month start)
// - endDate: "YYYY-MM-DD" (defaults to current month end)

// Response
{
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "totalExpenses": 500.00,
  "categoryBreakdown": [
    {
      "categoryId": 3,
      "categoryName": "Groceries",
      "totalAmount": 250.00,
      "expenseCount": 8,
      "percentage": 50.00
    },
    {
      "categoryId": 1,
      "categoryName": "Transportation",
      "totalAmount": 150.00,
      "expenseCount": 12,
      "percentage": 30.00
    }
  ],
  "currency": { ... }
}
```

**Use cases:**
- Pie charts
- Bar charts
- Category filtering
- Spending analysis

---

### 🏷️ Category Management

#### Get All Categories
```javascript
GET /api/v1/categories

// Response
[
  {
    "id": 1,
    "name": "Food & Dining",
    "description": "Restaurants, groceries, etc."
  },
  ...
]
```

#### Create Category
```javascript
POST /api/v1/categories

// Request
{
  "name": "Transportation",
  "description": "Car, bus, taxi expenses"
}

// Response (201 Created): CategoryResponse
```

#### Update Category
```javascript
PUT /api/v1/categories/{id}

// Request
{
  "name": "Updated Name",
  "description": "Updated description"
}

// Response: CategoryResponse
```

#### Delete Category
```javascript
DELETE /api/v1/categories/{id}

// Response: 204 No Content
```

---

## Data Models

### Currency Object
```typescript
interface Currency {
  id: number;
  code: string;      // "USD", "EUR", etc.
  name: string;      // "US Dollar"
  symbol: string;    // "$"
}
```

**Available currencies:** USD, EUR, GBP, JPY, CNY, RUB, UAH, PLN, CHF, CAD, AUD, BRL, INR, KRW, MXN, SEK, NOK, DKK, TRY, ZAR

### Expense Object
```typescript
interface Expense {
  id: number;
  amount: number;
  date: string;           // "YYYY-MM-DD"
  description: string;
  categoryId: number;
}
```

### Category Object
```typescript
interface Category {
  id: number;
  name: string;
  description: string;
}
```

---

## Common Use Cases

### 1. Dashboard Page

```javascript
async function loadDashboard() {
  const token = localStorage.getItem('accessToken');
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };

  try {
    // Fetch wallet info and statistics in parallel
    const [walletRes, statsRes] = await Promise.all([
      fetch(`${BASE_URL}/user/wallet`, { headers }),
      fetch(`${BASE_URL}/expenses/statistics`, { headers })
    ]);

    const wallet = await walletRes.json();
    const stats = await statsRes.json();

    // Display data
    displayBalance(wallet.currentBalance, wallet.currency.symbol);
    displayStats({
      today: stats.todayExpenses,
      week: stats.weekExpenses,
      month: stats.monthExpenses,
      avgDaily: stats.averageDailyExpenses
    });

    // Show warning if needed
    if (wallet.lowBalanceWarning) {
      showLowBalanceWarning(wallet.currentBalance);
    }

  } catch (error) {
    console.error('Failed to load dashboard:', error);
  }
}
```

### 2. Add Expense Form

```javascript
async function addExpense(formData) {
  const token = localStorage.getItem('accessToken');

  try {
    const response = await fetch(`${BASE_URL}/expenses`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        amount: parseFloat(formData.amount),
        date: formData.date,
        description: formData.description,
        categoryId: parseInt(formData.categoryId)
      })
    });

    if (!response.ok) {
      const error = await response.json();

      // Handle insufficient balance
      if (error.message.includes('Insufficient balance')) {
        alert('⚠️ Cannot add expense: Your balance is too low');
        return null;
      }

      throw new Error(error.message);
    }

    const expense = await response.json();

    // Success feedback
    alert('✅ Expense added successfully!');

    // Refresh balance
    await loadBalance();

    return expense;

  } catch (error) {
    console.error('Failed to add expense:', error);
    alert('❌ Failed to add expense. Please try again.');
    return null;
  }
}
```

### 3. Category Breakdown Chart

```javascript
async function loadCategoryChart(startDate, endDate) {
  const token = localStorage.getItem('accessToken');

  // Build URL with optional date params
  const url = new URL(`${BASE_URL}/expenses/statistics/by-category`);
  if (startDate) url.searchParams.append('startDate', startDate);
  if (endDate) url.searchParams.append('endDate', endDate);

  try {
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    const data = await response.json();

    // Prepare data for chart library (e.g., Chart.js)
    const chartData = {
      labels: data.categoryBreakdown.map(cat => cat.categoryName),
      datasets: [{
        data: data.categoryBreakdown.map(cat => cat.totalAmount),
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF']
      }]
    };

    renderPieChart(chartData);

  } catch (error) {
    console.error('Failed to load category chart:', error);
  }
}
```

### 4. Withdraw Money

```javascript
async function withdrawMoney(amount, description) {
  const token = localStorage.getItem('accessToken');

  try {
    const response = await fetch(`${BASE_URL}/user/withdraw`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        amount: parseFloat(amount),
        description: description || 'Withdrawal'
      })
    });

    if (!response.ok) {
      const error = await response.json();

      if (error.message.includes('Insufficient balance')) {
        alert(`⚠️ Cannot withdraw: ${error.message}`);
        return null;
      }

      throw new Error(error.message);
    }

    alert('✅ Withdrawal successful!');
    await loadBalance(); // Refresh balance display

    return await response.json();

  } catch (error) {
    console.error('Withdrawal failed:', error);
    alert('❌ Withdrawal failed. Please try again.');
    return null;
  }
}
```

### 5. Real-time Balance Display

```javascript
class BalanceManager {
  constructor() {
    this.balance = 0;
    this.currency = { symbol: '$' };
  }

  async fetchBalance() {
    const token = localStorage.getItem('accessToken');

    try {
      const response = await fetch(`${BASE_URL}/user/wallet`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      const wallet = await response.json();
      this.balance = wallet.currentBalance;
      this.currency = wallet.currency;

      this.updateDisplay();

      return wallet;
    } catch (error) {
      console.error('Failed to fetch balance:', error);
    }
  }

  updateDisplay() {
    const balanceElement = document.getElementById('balance');
    balanceElement.textContent = `${this.currency.symbol}${this.balance.toFixed(2)}`;

    // Update color based on balance
    if (this.balance < 100) {
      balanceElement.classList.add('low-balance');
    } else {
      balanceElement.classList.remove('low-balance');
    }
  }

  // Call after any transaction
  async refresh() {
    await this.fetchBalance();
  }
}

// Usage
const balanceManager = new BalanceManager();
await balanceManager.fetchBalance();
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2024-12-04T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance. Current balance: 50.00, Required: 100.00",
  "path": "/api/v1/expenses"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2024-12-04T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is invalid or expired",
  "path": "/api/v1/user/wallet"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-12-04T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found",
  "path": "/api/v1/expenses/999"
}
```

### Error Handling Helper

```javascript
async function apiRequest(url, options = {}) {
  const token = localStorage.getItem('accessToken');

  const defaultOptions = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };

  const mergedOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };

  try {
    const response = await fetch(url, mergedOptions);

    // Handle 401 - redirect to login
    if (response.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
      throw new Error('Session expired. Please login again.');
    }

    // Handle other errors
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Request failed');
    }

    // Handle 204 No Content
    if (response.status === 204) {
      return null;
    }

    return await response.json();

  } catch (error) {
    console.error('API Request failed:', error);
    throw error;
  }
}

// Usage
try {
  const wallet = await apiRequest(`${BASE_URL}/user/wallet`);
  console.log('Wallet:', wallet);
} catch (error) {
  alert(`Error: ${error.message}`);
}
```

---

## Best Practices

### 1. Token Management

```javascript
// Store token after login
function saveToken(authResponse) {
  localStorage.setItem('accessToken', authResponse.accessToken);
  localStorage.setItem('refreshToken', authResponse.refreshToken);
  localStorage.setItem('userId', authResponse.userId);
}

// Clear token on logout
function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  window.location.href = '/login';
}

// Check if user is authenticated
function isAuthenticated() {
  return !!localStorage.getItem('accessToken');
}
```

### 2. Date Formatting

```javascript
// Format date for API (YYYY-MM-DD)
function formatDateForAPI(date) {
  return date.toISOString().split('T')[0];
}

// Usage
const today = formatDateForAPI(new Date());  // "2024-12-04"
```

### 3. Amount Formatting

```javascript
// Format amount for display
function formatAmount(amount, currencySymbol) {
  return `${currencySymbol}${amount.toFixed(2)}`;
}

// Parse amount from input
function parseAmount(value) {
  return parseFloat(value) || 0;
}

// Validate amount
function isValidAmount(value) {
  const amount = parseFloat(value);
  return !isNaN(amount) && amount > 0;
}
```

### 4. Loading States

```javascript
// Button with loading state
function setButtonLoading(button, isLoading) {
  if (isLoading) {
    button.disabled = true;
    button.dataset.originalText = button.textContent;
    button.textContent = 'Loading...';
  } else {
    button.disabled = false;
    button.textContent = button.dataset.originalText;
  }
}

// Usage
const submitButton = document.getElementById('submit');
setButtonLoading(submitButton, true);

try {
  await addExpense(formData);
} finally {
  setButtonLoading(submitButton, false);
}
```

### 5. Debouncing API Calls

```javascript
// Debounce function for search/filter
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Usage
const searchExpenses = debounce(async (query) => {
  const expenses = await apiRequest(`${BASE_URL}/expenses`);
  // Filter and display results
}, 300);
```

### 6. Optimistic Updates

```javascript
// Update UI immediately, revert if API fails
async function deleteExpenseWithOptimisticUpdate(expenseId) {
  // 1. Save current state
  const expenseElement = document.getElementById(`expense-${expenseId}`);
  const originalHTML = expenseElement.innerHTML;

  // 2. Update UI immediately
  expenseElement.style.opacity = '0.5';

  try {
    // 3. Make API call
    await apiRequest(`${BASE_URL}/expenses/${expenseId}`, {
      method: 'DELETE'
    });

    // 4. Remove from DOM on success
    expenseElement.remove();

    // 5. Refresh balance
    await balanceManager.refresh();

  } catch (error) {
    // 6. Revert on failure
    expenseElement.innerHTML = originalHTML;
    expenseElement.style.opacity = '1';
    alert('Failed to delete expense');
  }
}
```

---

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" button at the top
3. Login to get a token
4. Enter token as: `Bearer your_token_here`
5. Click "Authorize" and "Close"
6. Now you can test all endpoints interactively

---

## Quick Reference

### Date Range for Current Month
```javascript
const now = new Date();
const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);

const startDate = formatDateForAPI(startOfMonth);
const endDate = formatDateForAPI(endOfMonth);
```

### Date Range for Current Week
```javascript
const now = new Date();
const dayOfWeek = now.getDay();
const diffToMonday = (dayOfWeek === 0 ? -6 : 1) - dayOfWeek;

const monday = new Date(now);
monday.setDate(now.getDate() + diffToMonday);

const sunday = new Date(monday);
sunday.setDate(monday.getDate() + 6);

const startDate = formatDateForAPI(monday);
const endDate = formatDateForAPI(sunday);
```

---

## Support & Resources

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Project Documentation:** [CLAUDE.md](CLAUDE.md)
- **New Features Guide:** [API_NEW_FEATURES.md](API_NEW_FEATURES.md)
- **Backend Repository:** Contact your backend team

---

## Contact

For questions or issues:
1. Check Swagger UI documentation
2. Review error messages in API responses
3. Contact backend team
4. Create an issue in the project repository

---

**Last Updated:** December 2024
