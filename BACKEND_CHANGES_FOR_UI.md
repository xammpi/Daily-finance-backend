# Backend Changes for UI Integration

**Date**: December 2024
**Version**: 1.1.0
**Breaking Changes**: Yes

## Overview

The backend has been refactored to implement a wallet-based financial system with comprehensive statistics tracking. This document outlines all changes that require UI updates.

---

## 🔴 Breaking Changes

### 1. Currency System Changed (Enum → Entity)

**Before:**
```json
{
  "currency": "USD"  // Simple string enum
}
```

**After:**
```json
{
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

**UI Impact:**
- Update all currency displays to use `currency.symbol` or `currency.code`
- Currency selectors should fetch from `/api/v1/currencies` endpoint (if available)
- 20 currencies now supported: USD, EUR, GBP, JPY, CNY, RUB, UAH, PLN, CHF, CAD, AUD, BRL, INR, KRW, MXN, SEK, NOK, DKK, TRY, ZAR

---

### 2. User Registration - Currency Selection Required

**Endpoint:** `POST /api/v1/auth/register`

**Before:**
```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**After:**
```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "currencyId": 1  // ⚠️ REQUIRED - Currency ID from currencies table
}
```

**UI Changes Required:**
- Add currency selector to registration form
- Fetch available currencies (or hardcode the 20 supported currencies)
- Set default to USD (id: 1) if user doesn't select

**Available Currencies (for hardcoding):**
```javascript
const CURRENCIES = [
  { id: 1, code: "USD", name: "US Dollar", symbol: "$" },
  { id: 2, code: "EUR", name: "Euro", symbol: "€" },
  { id: 3, code: "GBP", name: "British Pound", symbol: "£" },
  { id: 4, code: "JPY", name: "Japanese Yen", symbol: "¥" },
  { id: 5, code: "CNY", name: "Chinese Yuan", symbol: "¥" },
  { id: 6, code: "RUB", name: "Russian Ruble", symbol: "₽" },
  { id: 7, code: "UAH", name: "Ukrainian Hryvnia", symbol: "₴" },
  { id: 8, code: "PLN", name: "Polish Zloty", symbol: "zł" },
  { id: 9, code: "CHF", name: "Swiss Franc", symbol: "CHF" },
  { id: 10, code: "CAD", name: "Canadian Dollar", symbol: "C$" },
  { id: 11, code: "AUD", name: "Australian Dollar", symbol: "A$" },
  { id: 12, code: "BRL", name: "Brazilian Real", symbol: "R$" },
  { id: 13, code: "INR", name: "Indian Rupee", symbol: "₹" },
  { id: 14, code: "KRW", name: "South Korean Won", symbol: "₩" },
  { id: 15, code: "MXN", name: "Mexican Peso", symbol: "$" },
  { id: 16, code: "SEK", name: "Swedish Krona", symbol: "kr" },
  { id: 17, code: "NOK", name: "Norwegian Krone", symbol: "kr" },
  { id: 18, code: "DKK", name: "Danish Krone", symbol: "kr" },
  { id: 19, code: "TRY", name: "Turkish Lira", symbol: "₺" },
  { id: 20, code: "ZAR", name: "South African Rand", symbol: "R" }
];
```

---

### 3. User Profile Response Structure Changed

**Endpoint:** `GET /api/v1/user/profile`

**Before:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "balance": 1500.00,
  "currency": "USD",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**After:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "wallet": {
    "id": 1,
    "amount": 1500.00,
    "currency": {
      "id": 1,
      "code": "USD",
      "name": "US Dollar",
      "symbol": "$",
      "createdAt": "2024-12-03T20:00:00",
      "updatedAt": "2024-12-03T20:00:00"
    },
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-12-03T22:00:00"
  },
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**UI Migration:**
```javascript
// Before
const balance = user.balance;
const currency = user.currency; // "USD"

// After
const balance = user.wallet.amount;
const currency = user.wallet.currency.code; // "USD"
const currencySymbol = user.wallet.currency.symbol; // "$"
```

---

### 4. Balance Summary Response Changed

**Endpoint:** `GET /api/v1/user/balance-summary`

**Before:**
```json
{
  "currentBalance": 1500.00,
  "totalExpensesThisMonth": 450.00,
  "remainingBalance": 1500.00,
  "currency": "USD"
}
```

**After:**
```json
{
  "currentBalance": 1500.00,
  "totalExpensesThisMonth": 450.00,
  "remainingBalance": 1500.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

---

## ✨ New Features

### 1. Statistics Endpoints

#### Overall Statistics
**Endpoint:** `GET /api/v1/statistics/overall`

**Response:**
```json
{
  "currentBalance": 1500.00,
  "totalDeposits": 5000.00,
  "totalExpenses": 3500.00,
  "totalDepositsThisMonth": 1000.00,
  "totalExpensesThisMonth": 450.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

**Use Cases:**
- Dashboard overview
- Financial summary widgets
- Lifetime statistics

---

#### Category-wise Statistics
**Endpoint:** `GET /api/v1/statistics/by-category`

**Response:**
```json
[
  {
    "categoryId": 1,
    "categoryName": "Food & Dining",
    "totalAmount": 850.50,
    "expenseCount": 23
  },
  {
    "categoryId": 2,
    "categoryName": "Transportation",
    "totalAmount": 320.00,
    "expenseCount": 12
  },
  {
    "categoryId": 3,
    "categoryName": "Entertainment",
    "totalAmount": 180.75,
    "expenseCount": 8
  }
]
```

**Use Cases:**
- Pie charts showing expense breakdown
- Category insights
- Budget planning
- Spending patterns analysis

**Sorting:** Results are ordered by `totalAmount DESC` (highest spending first)

---

#### Daily Statistics
**Endpoint:** `GET /api/v1/statistics/daily`

**Response:**
```json
{
  "period": "daily",
  "currentBalance": 1500.00,
  "totalDeposits": 200.00,
  "totalExpenses": 85.50,
  "netChange": 114.50,
  "depositCount": 2,
  "expenseCount": 5,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

**Use Cases:**
- Today's activity summary
- Daily spending tracker
- Quick overview widget

---

#### Monthly Statistics
**Endpoint:** `GET /api/v1/statistics/monthly`

**Response:**
```json
{
  "period": "monthly",
  "currentBalance": 1500.00,
  "totalDeposits": 1000.00,
  "totalExpenses": 450.00,
  "netChange": 550.00,
  "depositCount": 8,
  "expenseCount": 32,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

**Use Cases:**
- Monthly spending reports
- Budget tracking
- Savings goals

**Note:** "This month" = current calendar month (from 1st to last day of month)

---

#### Yearly Statistics
**Endpoint:** `GET /api/v1/statistics/yearly`

**Response:**
```json
{
  "period": "yearly",
  "currentBalance": 1500.00,
  "totalDeposits": 12000.00,
  "totalExpenses": 10500.00,
  "netChange": 1500.00,
  "depositCount": 96,
  "expenseCount": 384,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "createdAt": "2024-12-03T20:00:00",
    "updatedAt": "2024-12-03T20:00:00"
  }
}
```

**Use Cases:**
- Annual financial reports
- Year-over-year comparisons
- Tax preparation

**Note:** "This year" = current calendar year (January 1 - December 31)

---

### 2. Period Statistics Fields Explained

| Field | Description |
|-------|-------------|
| `period` | Time period: "daily", "monthly", or "yearly" |
| `currentBalance` | Current wallet balance (same across all periods) |
| `totalDeposits` | Sum of all deposits in the period |
| `totalExpenses` | Sum of all expenses in the period |
| `netChange` | Deposits - Expenses (positive = gained, negative = lost) |
| `depositCount` | Number of deposit transactions in the period |
| `expenseCount` | Number of expense transactions in the period |
| `currency` | User's wallet currency (full object) |

---

## 🔧 Modified Endpoints

### Deposit Money
**Endpoint:** `POST /api/v1/user/deposit`

**Request:** (Unchanged)
```json
{
  "amount": 500.00
}
```

**Response:** (Changed - now returns full user profile with wallet)
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "wallet": {
    "id": 1,
    "amount": 2000.00,  // Updated balance
    "currency": {
      "id": 1,
      "code": "USD",
      "name": "US Dollar",
      "symbol": "$",
      "createdAt": "2024-12-03T20:00:00",
      "updatedAt": "2024-12-03T20:00:00"
    },
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-12-03T22:30:00"
  },
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**Backend Behavior:**
- Creates a `Deposit` record in the database
- Updates wallet balance
- Returns updated user profile

---

### Update Currency
**Endpoint:** `PUT /api/v1/user/currency`

**Request:**
```json
{
  "currencyId": 2  // Changed from currency code to currency ID
}
```

**Response:** Full user profile with updated wallet currency

**Before:** Accepted currency code (e.g., "EUR")
**After:** Accepts currency ID (e.g., 2)

---

## 📊 UI Implementation Guide

### Dashboard Overview Page

```typescript
// Fetch overall statistics
const stats = await fetch('/api/v1/statistics/overall');

// Display:
// - Current Balance: ${stats.currentBalance} {stats.currency.symbol}
// - Total Deposits (All Time): ${stats.totalDeposits}
// - Total Expenses (All Time): ${stats.totalExpenses}
// - This Month Deposits: ${stats.totalDepositsThisMonth}
// - This Month Expenses: ${stats.totalExpensesThisMonth}
```

### Statistics Page

```typescript
// Fetch period statistics
const daily = await fetch('/api/v1/statistics/daily');
const monthly = await fetch('/api/v1/statistics/monthly');
const yearly = await fetch('/api/v1/statistics/yearly');

// Display tabs or cards for each period showing:
// - Period name
// - Total deposits/expenses
// - Net change (with color: green if positive, red if negative)
// - Transaction counts
```

### Category Analytics

```typescript
// Fetch category statistics
const categories = await fetch('/api/v1/statistics/by-category');

// Create pie/donut chart:
categories.forEach(cat => {
  // Segment label: cat.categoryName
  // Segment value: cat.totalAmount
  // Tooltip: `${cat.expenseCount} expenses totaling ${cat.totalAmount}`
});

// Display top spending categories
const topCategories = categories.slice(0, 5);
```

### User Profile Page

```typescript
// Fetch user profile
const user = await fetch('/api/v1/user/profile');

// Display:
// - Balance: {user.wallet.amount} {user.wallet.currency.symbol}
// - Currency: {user.wallet.currency.name} ({user.wallet.currency.code})

// Currency change form:
// - Dropdown with all 20 currencies
// - On submit: PUT /api/v1/user/currency { currencyId: selectedId }
```

---

## 🎨 UI Components Suggestions

### Balance Display Component

```typescript
interface BalanceProps {
  amount: number;
  currency: Currency;
  label?: string;
}

function BalanceDisplay({ amount, currency, label }: BalanceProps) {
  return (
    <div>
      {label && <span>{label}</span>}
      <span>{currency.symbol}{amount.toFixed(2)}</span>
      <span>{currency.code}</span>
    </div>
  );
}
```

### Statistics Card Component

```typescript
interface StatsCardProps {
  period: 'daily' | 'monthly' | 'yearly';
  stats: PeriodStatistics;
}

function StatsCard({ period, stats }: StatsCardProps) {
  const isPositive = stats.netChange >= 0;

  return (
    <div>
      <h3>{period.charAt(0).toUpperCase() + period.slice(1)} Statistics</h3>
      <BalanceDisplay amount={stats.totalDeposits} currency={stats.currency} label="Deposits" />
      <BalanceDisplay amount={stats.totalExpenses} currency={stats.currency} label="Expenses" />
      <div style={{ color: isPositive ? 'green' : 'red' }}>
        Net: {stats.currency.symbol}{stats.netChange.toFixed(2)}
      </div>
      <div>
        {stats.depositCount} deposits, {stats.expenseCount} expenses
      </div>
    </div>
  );
}
```

---

## 🔐 Authentication

**No changes** to authentication endpoints or JWT handling. All statistics endpoints require Bearer token authentication.

---

## 📝 TypeScript Interfaces

```typescript
interface Currency {
  id: number;
  code: string;
  name: string;
  symbol: string;
  createdAt: string;
  updatedAt: string;
}

interface Wallet {
  id: number;
  amount: number;
  currency: Currency;
  createdAt: string;
  updatedAt: string;
}

interface UserProfile {
  id: number;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  wallet: Wallet;
  createdAt: string;
  updatedAt: string;
}

interface BalanceSummary {
  currentBalance: number;
  totalExpensesThisMonth: number;
  remainingBalance: number;
  currency: Currency;
}

interface StatisticsResponse {
  currentBalance: number;
  totalDeposits: number;
  totalExpenses: number;
  totalDepositsThisMonth: number;
  totalExpensesThisMonth: number;
  currency: Currency;
}

interface CategoryExpenseStatistics {
  categoryId: number;
  categoryName: string;
  totalAmount: number;
  expenseCount: number;
}

interface PeriodStatistics {
  period: 'daily' | 'monthly' | 'yearly';
  currentBalance: number;
  totalDeposits: number;
  totalExpenses: number;
  netChange: number;
  depositCount: number;
  expenseCount: number;
  currency: Currency;
}

interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  currencyId: number; // ⚠️ NEW REQUIRED FIELD
}

interface UpdateCurrencyRequest {
  currencyId: number; // Changed from currencyCode: string
}
```

---

## 🚨 Migration Checklist for UI Team

- [ ] Update `RegisterRequest` to include `currencyId` field
- [ ] Add currency selector to registration form
- [ ] Update user profile display to use `user.wallet.amount` instead of `user.balance`
- [ ] Update currency display to use `user.wallet.currency` object
- [ ] Implement statistics dashboard with all 5 endpoints
- [ ] Create category breakdown visualization
- [ ] Update all balance displays to format with currency symbol
- [ ] Change currency update flow to use currency ID instead of code
- [ ] Update TypeScript interfaces to match new response structures
- [ ] Test all flows with different currencies
- [ ] Add period statistics widgets (daily/monthly/yearly)
- [ ] Implement net change indicators with color coding

---

## 📞 Support

For questions or issues, please check:
- Full API documentation: `/swagger-ui.html`
- OpenAPI spec: `/v3/api-docs`
- Backend repository issues

---

## 🔄 Version History

**v1.1.0** (December 2024)
- Wallet system implementation
- Currency changed from enum to entity
- Statistics endpoints added
- Deposit tracking implemented
- Breaking changes to user profile structure

**v1.0.0** (Initial Release)
- Basic expense tracking
- User authentication
- Category management
