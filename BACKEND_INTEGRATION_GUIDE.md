# Backend Integration Guide for Frontend Team

**Daily Finance Backend - How It Works**

This guide explains the backend architecture, workflows, and best practices for frontend integration.

---

## 📚 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Authentication Flow](#authentication-flow)
3. [Wallet System](#wallet-system)
4. [Expense Management](#expense-management)
5. [Statistics System](#statistics-system)
6. [Common Workflows](#common-workflows)
7. [Error Handling](#error-handling)
8. [Best Practices](#best-practices)

---

## 🏗️ Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────┐
│                   FRONTEND (UI)                      │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/REST API
                       │ JSON
                       ↓
┌─────────────────────────────────────────────────────┐
│              BACKEND (Spring Boot)                   │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ Controllers  │→ │  Services    │→ │Repository │ │
│  │ (REST API)   │  │ (Business    │  │ (Database)│ │
│  │              │  │  Logic)      │  │           │ │
│  └──────────────┘  └──────────────┘  └───────────┘ │
│                                                      │
│  Authentication: JWT (Bearer Token)                 │
│  API Docs: Swagger UI at /swagger-ui.html          │
└──────────────────────┬──────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────┐
│              DATABASE (PostgreSQL)                   │
│                                                      │
│  Tables: users, wallets, currencies, deposits,      │
│          expenses, categories                       │
└─────────────────────────────────────────────────────┘
```

### Database Schema

```
users (id, email, username, password, firstName, lastName, enabled)
  ↓ 1:1
wallets (id, user_id, amount, currency_id)
  ↓ N:1
currencies (id, code, name, symbol)

users → deposits (id, user_id, amount, date, description)
users → expenses (id, user_id, category_id, amount, date, description)
users → categories (id, user_id, name, description)
```

---

## 🔐 Authentication Flow

### How Authentication Works

1. **User Registration**
   - User provides: email, username, password, firstName, lastName, currencyId
   - Backend creates: User record + Wallet record (automatically)
   - Wallet starts with 0.00 balance in selected currency
   - Returns: JWT token + user profile

2. **User Login**
   - User provides: username/email + password
   - Backend validates credentials
   - Returns: JWT token + user profile

3. **API Requests**
   - All requests (except auth endpoints) require JWT token
   - Token sent in header: `Authorization: Bearer <token>`
   - Token contains: user ID, username, expiration time
   - Token expires: 24 hours (configurable)

### Authentication Endpoints

```
POST /api/v1/auth/register  (Public)
POST /api/v1/auth/login     (Public)

All other /api/v1/** endpoints require authentication
```

### Frontend Implementation

```typescript
// 1. Register or Login
const response = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'john_doe',
    password: 'SecurePass123!'
  })
});

const data = await response.json();
// data = { token: "eyJhbGc...", user: {...} }

// 2. Store token (localStorage, sessionStorage, or cookie)
localStorage.setItem('authToken', data.token);
localStorage.setItem('user', JSON.stringify(data.user));

// 3. Use token in all subsequent requests
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
  'Content-Type': 'application/json'
};

const profile = await fetch('/api/v1/user/profile', { headers });
```

---

## 💰 Wallet System

### How Wallets Work

**Concept:**
- Each user has exactly **one wallet**
- Wallet is created automatically during registration
- Wallet has a **balance** and **currency**
- All money operations go through the wallet

**Money Flow:**

```
┌─────────────┐
│   Deposit   │ → Adds money to wallet
└─────────────┘
      ↓
┌─────────────┐
│   Wallet    │ ← Current balance
└─────────────┘
      ↓
┌─────────────┐
│   Expense   │ → Deducts money from wallet
└─────────────┘
```

### Wallet Operations

#### 1. View Current Balance

```typescript
// Get user profile (includes wallet)
const response = await fetch('/api/v1/user/profile', { headers });
const user = await response.json();

console.log(user.wallet.amount);              // 1500.00
console.log(user.wallet.currency.symbol);     // "$"
console.log(user.wallet.currency.code);       // "USD"
```

#### 2. Deposit Money

```typescript
// Add money to wallet
const response = await fetch('/api/v1/user/deposit', {
  method: 'POST',
  headers,
  body: JSON.stringify({ amount: 500.00 })
});

const updatedUser = await response.json();
console.log(updatedUser.wallet.amount); // 2000.00 (1500 + 500)
```

**Backend Process:**
1. Creates a `Deposit` record (for history tracking)
2. Adds amount to wallet balance
3. Returns updated user profile

#### 3. Change Currency

```typescript
// Change wallet currency (e.g., USD → EUR)
const response = await fetch('/api/v1/user/currency', {
  method: 'PUT',
  headers,
  body: JSON.stringify({ currencyId: 2 }) // 2 = EUR
});

const updatedUser = await response.json();
console.log(updatedUser.wallet.currency.code); // "EUR"
console.log(updatedUser.wallet.currency.symbol); // "€"
```

**⚠️ Important:** Changing currency does NOT convert the amount. If user had $100 USD and changes to EUR, they'll have 100 EUR.

---

## 📝 Expense Management

### How Expenses Work

**Process:**

1. User creates expense with amount, category, date, description
2. Backend **automatically deducts** amount from wallet
3. Expense is saved to database
4. Balance is updated immediately

**Rules:**

- ✅ Can create expense even if balance goes negative (no validation)
- ✅ Updating expense adjusts balance (refunds old amount, deducts new amount)
- ✅ Deleting expense refunds money back to wallet

### Expense Operations

#### 1. Create Expense

```typescript
const response = await fetch('/api/v1/expenses', {
  method: 'POST',
  headers,
  body: JSON.stringify({
    amount: 45.50,
    categoryId: 1,
    date: '2024-12-03',
    description: 'Lunch at restaurant'
  })
});

const expense = await response.json();

// What happens in backend:
// 1. wallet.amount -= 45.50
// 2. expense record created
// 3. expense returned
```

#### 2. Update Expense

```typescript
// Change expense from $45.50 to $50.00
const response = await fetch('/api/v1/expenses/123', {
  method: 'PUT',
  headers,
  body: JSON.stringify({
    amount: 50.00,
    categoryId: 1,
    date: '2024-12-03',
    description: 'Lunch at restaurant (updated)'
  })
});

// What happens in backend:
// 1. wallet.amount += 45.50 (refund old amount)
// 2. wallet.amount -= 50.00 (deduct new amount)
// 3. expense updated
// Net effect: wallet.amount -= 4.50
```

#### 3. Delete Expense

```typescript
const response = await fetch('/api/v1/expenses/123', {
  method: 'DELETE',
  headers
});

// What happens in backend:
// 1. wallet.amount += 45.50 (refund)
// 2. expense deleted
```

#### 4. List Expenses

```typescript
// Get all user's expenses
const response = await fetch('/api/v1/expenses', { headers });
const expenses = await response.json();

// Response:
// [
//   {
//     id: 1,
//     amount: 45.50,
//     date: '2024-12-03',
//     description: 'Lunch',
//     category: { id: 1, name: 'Food & Dining', ... },
//     user: { id: 1, username: 'john_doe', ... },
//     createdAt: '...',
//     updatedAt: '...'
//   },
//   ...
// ]
```

---

## 📊 Statistics System

### How Statistics are Calculated

**Real-Time Calculation:**
- Statistics are calculated on-demand (not cached)
- Data is aggregated from `deposits` and `expenses` tables
- Filtered by date ranges for period statistics

### Statistics Breakdown

#### 1. Overall Statistics

**What it shows:** Lifetime totals + current month summary

```typescript
const response = await fetch('/api/v1/statistics/overall', { headers });
const stats = await response.json();

// {
//   currentBalance: 1500.00,        // Current wallet balance
//   totalDeposits: 5000.00,         // All-time deposits
//   totalExpenses: 3500.00,         // All-time expenses
//   totalDepositsThisMonth: 1000.00,// Current month deposits
//   totalExpensesThisMonth: 450.00, // Current month expenses
//   currency: { ... }
// }
```

**Use Case:** Dashboard overview showing lifetime and monthly summaries

#### 2. Category Statistics

**What it shows:** Expense breakdown by category (all-time)

```typescript
const response = await fetch('/api/v1/statistics/by-category', { headers });
const categories = await response.json();

// [
//   {
//     categoryId: 1,
//     categoryName: 'Food & Dining',
//     totalAmount: 850.50,   // Total spent in this category
//     expenseCount: 23       // Number of expenses
//   },
//   ...
// ]
// Sorted by totalAmount DESC (highest spending first)
```

**Use Case:** Pie charts, category insights, spending patterns

#### 3. Period Statistics (Daily/Monthly/Yearly)

**What it shows:** Detailed breakdown for a specific time period

```typescript
// Today's activity
const daily = await fetch('/api/v1/statistics/daily', { headers });

// This month (1st to last day of current month)
const monthly = await fetch('/api/v1/statistics/monthly', { headers });

// This year (January 1 to December 31)
const yearly = await fetch('/api/v1/statistics/yearly', { headers });

const stats = await daily.json();
// {
//   period: 'daily',
//   currentBalance: 1500.00,
//   totalDeposits: 200.00,     // Deposits in period
//   totalExpenses: 85.50,      // Expenses in period
//   netChange: 114.50,         // Deposits - Expenses
//   depositCount: 2,           // Number of deposits
//   expenseCount: 5,           // Number of expenses
//   currency: { ... }
// }
```

**Net Change Indicator:**
- Positive (green): You gained money (deposits > expenses)
- Negative (red): You lost money (expenses > deposits)
- Zero: Break-even

---

## 🔄 Common Workflows

### Workflow 1: New User Onboarding

```typescript
// Step 1: User selects currency during registration
const currencies = [
  { id: 1, code: 'USD', symbol: '$' },
  { id: 2, code: 'EUR', symbol: '€' },
  // ... (hardcode or fetch from backend)
];

// Step 2: Register with selected currency
const response = await fetch('/api/v1/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'user@example.com',
    username: 'john_doe',
    password: 'SecurePass123!',
    firstName: 'John',
    lastName: 'Doe',
    currencyId: 1  // USD
  })
});

const { token, user } = await response.json();

// Step 3: Store auth token
localStorage.setItem('authToken', token);

// Step 4: User now has:
// - Account created
// - Wallet created with 0.00 USD
// - Ready to deposit money and track expenses
```

### Workflow 2: Adding First Expense

```typescript
// User flow:
// 1. User logs in
// 2. Deposits initial money
// 3. Creates categories
// 4. Adds expense

// Step 1: Deposit money
await fetch('/api/v1/user/deposit', {
  method: 'POST',
  headers,
  body: JSON.stringify({ amount: 1000.00 })
});
// Balance: 0 → 1000

// Step 2: Create category
const categoryRes = await fetch('/api/v1/categories', {
  method: 'POST',
  headers,
  body: JSON.stringify({
    name: 'Food & Dining',
    description: 'Restaurants, groceries, etc.'
  })
});
const category = await categoryRes.json();

// Step 3: Add expense
await fetch('/api/v1/expenses', {
  method: 'POST',
  headers,
  body: JSON.stringify({
    amount: 45.50,
    categoryId: category.id,
    date: '2024-12-03',
    description: 'Lunch'
  })
});
// Balance: 1000 → 954.50
```

### Workflow 3: Monthly Budget Review

```typescript
// Step 1: Get monthly statistics
const monthlyStats = await fetch('/api/v1/statistics/monthly', { headers });
const stats = await monthlyStats.json();

// Step 2: Get category breakdown
const categoryStats = await fetch('/api/v1/statistics/by-category', { headers });
const categories = await categoryStats.json();

// Step 3: Display report
console.log(`Monthly Report for ${getCurrentMonth()}`);
console.log(`Total Deposits: ${stats.totalDeposits}`);
console.log(`Total Expenses: ${stats.totalExpenses}`);
console.log(`Net Change: ${stats.netChange}`);
console.log(`\nTop Spending Categories:`);
categories.slice(0, 5).forEach(cat => {
  console.log(`  ${cat.categoryName}: ${cat.totalAmount} (${cat.expenseCount} expenses)`);
});
```

### Workflow 4: Expense Correction

```typescript
// User realizes they entered wrong amount
// Original expense: $100, should be $80

// Step 1: Get expense
const expense = await fetch('/api/v1/expenses/123', { headers });
const expenseData = await expense.json();

// Step 2: Update expense
await fetch('/api/v1/expenses/123', {
  method: 'PUT',
  headers,
  body: JSON.stringify({
    ...expenseData,
    amount: 80.00  // Changed from 100.00
  })
});

// Backend automatically:
// - Refunds 100.00 to wallet
// - Deducts 80.00 from wallet
// - Net effect: wallet gains 20.00
```

---

## ⚠️ Error Handling

### Common HTTP Status Codes

| Code | Meaning | When it happens |
|------|---------|-----------------|
| 200 | Success | Request completed successfully |
| 201 | Created | Resource created (POST requests) |
| 400 | Bad Request | Invalid request data (validation failed) |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Backend error (bug or database issue) |

### Error Response Format

```json
{
  "timestamp": "2024-12-03T22:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero",
  "path": "/api/v1/user/deposit",
  "details": ["amount: must be greater than 0"]
}
```

### Frontend Error Handling

```typescript
async function apiRequest(url: string, options: RequestInit) {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    });

    // Handle unauthorized (token expired or invalid)
    if (response.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
      throw new Error('Session expired. Please login again.');
    }

    // Handle other errors
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Request failed');
    }

    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

// Usage
try {
  const stats = await apiRequest('/api/v1/statistics/overall', {
    method: 'GET'
  });
  displayStats(stats);
} catch (error) {
  showErrorToast(error.message);
}
```

### Validation Errors

**Common validation rules:**

- Email: Must be valid format
- Username: 3-50 characters, alphanumeric + underscore
- Password: Minimum 8 characters
- Amount: Must be positive number
- Date: Must be valid date format (YYYY-MM-DD)
- CategoryId: Must exist in database

---

## ✅ Best Practices

### 1. Token Management

```typescript
// ✅ Good: Refresh user data periodically
setInterval(async () => {
  const user = await fetch('/api/v1/user/profile', { headers });
  updateUserState(await user.json());
}, 5 * 60 * 1000); // Every 5 minutes

// ❌ Bad: Storing sensitive data in localStorage without encryption
localStorage.setItem('password', password); // Never do this!
```

### 2. Currency Display

```typescript
// ✅ Good: Use currency symbol and format correctly
function formatCurrency(amount: number, currency: Currency): string {
  return `${currency.symbol}${amount.toFixed(2)}`;
}

// Display: $45.50, €123.00, £99.99

// ❌ Bad: Hardcoding currency or wrong decimal places
const display = `$${amount}`; // Wrong if user uses EUR
```

### 3. Date Handling

```typescript
// ✅ Good: Use ISO format for API, localize for display
const apiDate = new Date().toISOString().split('T')[0]; // "2024-12-03"
const displayDate = new Date().toLocaleDateString();     // "12/3/2024"

// ❌ Bad: Inconsistent date formats
const date = '12/3/2024'; // Backend expects YYYY-MM-DD
```

### 4. Real-time Balance Updates

```typescript
// ✅ Good: Update balance immediately after operations
async function createExpense(data: ExpenseRequest) {
  const expense = await apiRequest('/api/v1/expenses', {
    method: 'POST',
    body: JSON.stringify(data)
  });

  // Refresh user profile to get updated balance
  const user = await apiRequest('/api/v1/user/profile', { method: 'GET' });
  updateBalance(user.wallet.amount);

  return expense;
}

// ❌ Bad: Waiting for manual refresh
// User creates expense, balance shows old value until page reload
```

### 5. Statistics Caching

```typescript
// ✅ Good: Cache statistics with expiration
const STATS_CACHE_TIME = 5 * 60 * 1000; // 5 minutes

async function getStatistics() {
  const cached = getFromCache('statistics');
  if (cached && Date.now() - cached.timestamp < STATS_CACHE_TIME) {
    return cached.data;
  }

  const stats = await apiRequest('/api/v1/statistics/overall', { method: 'GET' });
  setCache('statistics', { data: stats, timestamp: Date.now() });
  return stats;
}

// ❌ Bad: Fetching statistics on every render
// Causes unnecessary backend load
```

### 6. Category Management

```typescript
// ✅ Good: Fetch categories once and cache
const categories = await apiRequest('/api/v1/categories', { method: 'GET' });
// Store in global state (Redux, Context, etc.)

// When creating expense, use cached categories for dropdown
<select>
  {categories.map(cat => (
    <option key={cat.id} value={cat.id}>{cat.name}</option>
  ))}
</select>

// ❌ Bad: Fetching categories every time user opens expense form
```

### 7. Error Boundaries

```typescript
// ✅ Good: Handle errors gracefully with user-friendly messages
try {
  await createExpense(data);
  showSuccessToast('Expense created successfully');
} catch (error) {
  if (error.message.includes('Category not found')) {
    showErrorToast('Please select a valid category');
  } else {
    showErrorToast('Failed to create expense. Please try again.');
  }
}

// ❌ Bad: Showing technical errors to users
alert(error.stack); // Confusing for users
```

---

## 🔗 API Reference Quick Links

- **Full API Documentation**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

---

## 📋 API Endpoints Summary

### Authentication (Public)
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
```

### User & Wallet (Protected)
```
GET    /api/v1/user/profile
POST   /api/v1/user/deposit
PUT    /api/v1/user/currency
GET    /api/v1/user/balance-summary
```

### Expenses (Protected)
```
GET    /api/v1/expenses
POST   /api/v1/expenses
GET    /api/v1/expenses/{id}
PUT    /api/v1/expenses/{id}
DELETE /api/v1/expenses/{id}
```

### Categories (Protected)
```
GET    /api/v1/categories
POST   /api/v1/categories
GET    /api/v1/categories/{id}
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

### Statistics (Protected)
```
GET    /api/v1/statistics/overall
GET    /api/v1/statistics/by-category
GET    /api/v1/statistics/daily
GET    /api/v1/statistics/monthly
GET    /api/v1/statistics/yearly
```

---

## 🎯 Key Concepts Summary

### 1. One User → One Wallet
- Each user has exactly one wallet
- Wallet created automatically during registration
- Cannot create additional wallets

### 2. Automatic Balance Management
- Creating expense → deducts from wallet
- Updating expense → adjusts wallet balance
- Deleting expense → refunds to wallet
- Deposit → adds to wallet
- **You don't need to manually update balance**

### 3. Currency Selection
- Selected during registration (required)
- Can be changed later via PUT /user/currency
- Changing currency does NOT convert amounts
- All transactions use wallet's current currency

### 4. Real-time Statistics
- Calculated on-demand (not cached)
- Always up-to-date with latest data
- Period definitions:
  - Daily = Today
  - Monthly = Current calendar month (1st to last day)
  - Yearly = Current calendar year (Jan 1 to Dec 31)

### 5. Category Ownership
- Each user has their own categories
- Cannot see or use other users' categories
- Categories are created per-user basis

---

## 🚀 Getting Started Checklist

- [ ] Set up authentication flow (login/register)
- [ ] Implement JWT token storage and management
- [ ] Create currency selector for registration
- [ ] Build wallet/balance display component
- [ ] Implement deposit functionality
- [ ] Create expense form with category selection
- [ ] Build expense list view
- [ ] Implement statistics dashboard
- [ ] Add category breakdown visualization
- [ ] Implement period statistics (daily/monthly/yearly)
- [ ] Add error handling for all API calls
- [ ] Test with different currencies

---

## 💡 Tips for Frontend Development

1. **Use TypeScript** - All interfaces are provided in BACKEND_CHANGES_FOR_UI.md
2. **Test with real data** - Create deposits and expenses to see how balance updates
3. **Check Swagger UI** - Interactive API documentation with examples
4. **Monitor network tab** - See exact request/response formats
5. **Handle loading states** - Statistics can take a moment to calculate
6. **Cache wisely** - Categories rarely change, statistics change frequently
7. **Format currencies** - Always use currency symbol from wallet.currency.symbol
8. **Validate before submit** - Match backend validation rules to avoid errors

---

## 📞 Need Help?

- 📖 Full API changes: `BACKEND_CHANGES_FOR_UI.md`
- 🔗 Interactive docs: `/swagger-ui.html` when backend is running
- 🐛 Report issues: Backend repository issues section

---

## 📊 Example: Complete Dashboard Implementation

```typescript
import React, { useEffect, useState } from 'react';

function Dashboard() {
  const [user, setUser] = useState(null);
  const [overallStats, setOverallStats] = useState(null);
  const [monthlyStats, setMonthlyStats] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const headers = {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
          'Content-Type': 'application/json'
        };

        // Fetch all data in parallel
        const [userRes, overallRes, monthlyRes, categoriesRes] = await Promise.all([
          fetch('/api/v1/user/profile', { headers }),
          fetch('/api/v1/statistics/overall', { headers }),
          fetch('/api/v1/statistics/monthly', { headers }),
          fetch('/api/v1/statistics/by-category', { headers })
        ]);

        setUser(await userRes.json());
        setOverallStats(await overallRes.json());
        setMonthlyStats(await monthlyRes.json());
        setCategories(await categoriesRes.json());
      } catch (error) {
        console.error('Failed to load dashboard:', error);
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="dashboard">
      {/* Current Balance */}
      <div className="balance-card">
        <h2>Current Balance</h2>
        <div className="amount">
          {user.wallet.currency.symbol}{user.wallet.amount.toFixed(2)}
        </div>
        <div className="currency">{user.wallet.currency.name}</div>
      </div>

      {/* Monthly Summary */}
      <div className="monthly-summary">
        <h2>This Month</h2>
        <div className="stat">
          <span>Deposits:</span>
          <span className="positive">
            +{monthlyStats.currency.symbol}{monthlyStats.totalDeposits.toFixed(2)}
          </span>
        </div>
        <div className="stat">
          <span>Expenses:</span>
          <span className="negative">
            -{monthlyStats.currency.symbol}{monthlyStats.totalExpenses.toFixed(2)}
          </span>
        </div>
        <div className="stat">
          <span>Net Change:</span>
          <span className={monthlyStats.netChange >= 0 ? 'positive' : 'negative'}>
            {monthlyStats.currency.symbol}{monthlyStats.netChange.toFixed(2)}
          </span>
        </div>
      </div>

      {/* Category Breakdown */}
      <div className="categories">
        <h2>Top Spending Categories</h2>
        {categories.slice(0, 5).map(cat => (
          <div key={cat.categoryId} className="category-item">
            <span>{cat.categoryName}</span>
            <span>{user.wallet.currency.symbol}{cat.totalAmount.toFixed(2)}</span>
            <span className="count">({cat.expenseCount} expenses)</span>
          </div>
        ))}
      </div>

      {/* Lifetime Stats */}
      <div className="lifetime-stats">
        <h2>All Time</h2>
        <div>Total Deposits: {overallStats.currency.symbol}{overallStats.totalDeposits.toFixed(2)}</div>
        <div>Total Expenses: {overallStats.currency.symbol}{overallStats.totalExpenses.toFixed(2)}</div>
      </div>
    </div>
  );
}

export default Dashboard;
```

---

**Last Updated:** December 2024
**Backend Version:** 1.1.0
**API Base URL:** `http://localhost:8080/api/v1`
