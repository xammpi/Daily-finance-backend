# New API Features - December 2024

## Summary of New Endpoints and Features

### 1. Wallet Management Enhancements

#### Update Wallet Balance Directly
**Endpoint:** `PUT /api/v1/user/balance`

Allows users to directly set their wallet balance to any non-negative value.

**Request:**
```json
{
  "amount": 5000.00
}
```

**Response:** `200 OK` - Returns UserProfileResponse

---

#### Withdraw Money from Wallet
**Endpoint:** `POST /api/v1/user/withdraw`

Withdraw money from wallet with automatic balance validation.

**Request:**
```json
{
  "amount": 100.00,
  "description": "Cash withdrawal"
}
```

**Response:** `200 OK` - Returns UserProfileResponse

**Error:** `400 Bad Request` if insufficient balance

---

#### Get Detailed Wallet Information
**Endpoint:** `GET /api/v1/user/wallet`

Returns comprehensive wallet details including transaction summaries.

**Response:** `200 OK`
```json
{
  "walletId": 1,
  "currentBalance": 4500.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  },
  "totalDeposits": 5,
  "totalDepositAmount": 5000.00,
  "totalExpenses": 12,
  "totalExpenseAmount": 500.00,
  "lastTransactionDate": "2024-12-04T10:30:00",
  "lowBalanceWarning": false
}
```

---

### 2. Enhanced Balance Summary

**Endpoint:** `GET /api/v1/user/balance-summary`

Now includes today, week, and month expenses (previously only month).

**Response:** `200 OK`
```json
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

### 3. Comprehensive Expense Statistics

#### Get Expense Statistics
**Endpoint:** `GET /api/v1/expenses/statistics`

Returns comprehensive statistics including averages and comparisons with previous periods.

**Response:** `200 OK`
```json
{
  "todayExpenses": 50.00,
  "weekExpenses": 250.00,
  "monthExpenses": 500.00,
  "totalExpenses": 2500.00,
  "averageDailyExpenses": 25.50,
  "averageWeeklyExpenses": 178.50,
  "averageMonthlyExpenses": 625.00,
  "previousWeekExpenses": 300.00,
  "previousMonthExpenses": 550.00,
  "currency": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$"
  }
}
```

**Statistics Explained:**
- **todayExpenses**: Total spent today
- **weekExpenses**: Total spent this week (Monday-Sunday)
- **monthExpenses**: Total spent this month
- **totalExpenses**: All-time total expenses
- **averageDailyExpenses**: Total expenses / days since first expense
- **averageWeeklyExpenses**: Total expenses / weeks since first expense
- **averageMonthlyExpenses**: Total expenses / months since first expense
- **previousWeekExpenses**: Total spent last week (for comparison)
- **previousMonthExpenses**: Total spent last month (for comparison)

---

#### Get Category-Wise Statistics
**Endpoint:** `GET /api/v1/expenses/statistics/by-category`

Returns expense breakdown by category with percentages for a given date range.

**Query Parameters:**
- `startDate` (optional): Start date in ISO format (YYYY-MM-DD). Defaults to current month start.
- `endDate` (optional): End date in ISO format (YYYY-MM-DD). Defaults to current month end.

**Example Request:**
```
GET /api/v1/expenses/statistics/by-category?startDate=2024-12-01&endDate=2024-12-31
```

**Response:** `200 OK`
```json
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
    },
    {
      "categoryId": 2,
      "categoryName": "Entertainment",
      "totalAmount": 100.00,
      "expenseCount": 5,
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

**Notes:**
- Categories are sorted by total amount (highest spending first)
- Percentage shows what portion of total expenses went to each category
- If no date range specified, defaults to current month

---

### 4. Balance Validation

All operations that decrease the wallet balance now include validation:

#### Creating Expenses
**Endpoint:** `POST /api/v1/expenses`

Now validates that the user has sufficient balance before creating the expense.

**Error Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-12-04T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance. Current balance: 50.00, Required: 100.00",
  "path": "/api/v1/expenses"
}
```

#### Withdrawing Money
**Endpoint:** `POST /api/v1/user/withdraw`

Validates that the user has sufficient balance before processing the withdrawal.

**Error Response:** `400 Bad Request`
```json
{
  "timestamp": "2024-12-04T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance. Current balance: 50.00",
  "path": "/api/v1/user/withdraw"
}
```

---

### 5. Low Balance Warning

The wallet details endpoint (`GET /api/v1/user/wallet`) includes a `lowBalanceWarning` field:
- Returns `true` when balance is between 0 and 100 (exclusive)
- Returns `false` when balance is 100 or above
- Frontend can use this to display warnings to users

---

## Integration Examples

### Example 1: Dashboard with Statistics

```javascript
// Fetch wallet details and statistics for dashboard
async function loadDashboard() {
  const token = localStorage.getItem('accessToken');

  // Get wallet info
  const walletResponse = await fetch('http://localhost:8080/api/v1/user/wallet', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const wallet = await walletResponse.json();

  // Get expense statistics
  const statsResponse = await fetch('http://localhost:8080/api/v1/expenses/statistics', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const stats = await statsResponse.json();

  // Display warning if low balance
  if (wallet.lowBalanceWarning) {
    showWarning(`Low balance: ${wallet.currency.symbol}${wallet.currentBalance}`);
  }

  // Display statistics
  displayStats({
    balance: wallet.currentBalance,
    todaySpent: stats.todayExpenses,
    weekSpent: stats.weekExpenses,
    monthSpent: stats.monthExpenses,
    avgDaily: stats.averageDailyExpenses
  });
}
```

### Example 2: Category Breakdown Chart

```javascript
// Fetch category statistics for pie/bar chart
async function loadCategoryChart(startDate, endDate) {
  const token = localStorage.getItem('accessToken');

  const url = new URL('http://localhost:8080/api/v1/expenses/statistics/by-category');
  if (startDate) url.searchParams.append('startDate', startDate);
  if (endDate) url.searchParams.append('endDate', endDate);

  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const data = await response.json();

  // Create chart with percentages
  const chartData = data.categoryBreakdown.map(cat => ({
    label: cat.categoryName,
    value: cat.totalAmount,
    percentage: cat.percentage,
    count: cat.expenseCount
  }));

  renderPieChart(chartData);
}
```

### Example 3: Add Expense with Balance Check

```javascript
// Add expense with proper error handling
async function addExpense(amount, date, description, categoryId) {
  const token = localStorage.getItem('accessToken');

  try {
    const response = await fetch('http://localhost:8080/api/v1/expenses', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        amount,
        date,
        description,
        categoryId
      })
    });

    if (!response.ok) {
      const error = await response.json();
      if (error.message.includes('Insufficient balance')) {
        alert('Cannot add expense: Insufficient balance');
        return null;
      }
      throw new Error(error.message);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to add expense:', error);
    throw error;
  }
}
```

### Example 4: Withdraw Money

```javascript
// Withdraw money with validation
async function withdrawMoney(amount, description) {
  const token = localStorage.getItem('accessToken');

  try {
    const response = await fetch('http://localhost:8080/api/v1/user/withdraw', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        amount,
        description
      })
    });

    if (!response.ok) {
      const error = await response.json();
      if (error.message.includes('Insufficient balance')) {
        alert(`Cannot withdraw: ${error.message}`);
        return null;
      }
      throw new Error(error.message);
    }

    const result = await response.json();
    alert('Withdrawal successful!');
    return result;
  } catch (error) {
    console.error('Withdrawal failed:', error);
    throw error;
  }
}
```

---

## Migration Notes

### For Existing Frontend Implementations

1. **Balance Summary Response Changed**
   - Old: `{ currentBalance, totalExpensesThisMonth, currency }`
   - New: `{ currentBalance, todayExpenses, weekExpenses, monthExpenses, currency }`
   - **Action Required**: Update frontend to handle new fields

2. **New Endpoints Available**
   - `PUT /api/v1/user/balance` - Direct balance update
   - `POST /api/v1/user/withdraw` - Withdraw functionality
   - `GET /api/v1/user/wallet` - Detailed wallet info
   - `GET /api/v1/expenses/statistics` - Comprehensive statistics
   - `GET /api/v1/expenses/statistics/by-category` - Category breakdown

3. **Error Handling Enhanced**
   - Expense creation now returns `400 Bad Request` if insufficient balance
   - Withdrawal returns `400 Bad Request` if insufficient balance
   - **Action Required**: Add error handling for insufficient balance scenarios

---

## Testing the New Features

Use Swagger UI at `http://localhost:8080/swagger-ui.html` to test all new endpoints interactively.

### Quick Test Sequence:

1. Register/Login to get JWT token
2. Deposit money: `POST /api/v1/user/deposit`
3. Get wallet details: `GET /api/v1/user/wallet`
4. Create some expenses: `POST /api/v1/expenses`
5. View statistics: `GET /api/v1/expenses/statistics`
6. View category breakdown: `GET /api/v1/expenses/statistics/by-category`
7. Try withdrawal: `POST /api/v1/user/withdraw`

---

## Support

For additional information, see:
- [CLAUDE.md](CLAUDE.md) - Complete project documentation
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Full API reference
- Swagger UI: http://localhost:8080/swagger-ui.html
