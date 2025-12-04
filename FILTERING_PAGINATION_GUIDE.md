# Expense Filtering & Pagination Guide

> Complete guide for filtering and paginating expenses with multiple criteria

## Overview

The expense filtering endpoint allows you to:
- **Filter by category** - Show expenses from specific category
- **Filter by date range** - Show expenses between two dates
- **Filter by amount range** - Show expenses within min/max amounts
- **Paginate results** - Control page size and navigate through pages
- **Sort results** - Automatic sorting by date (newest first)

---

## Endpoint

```
GET /api/v1/expenses/filter
```

**Authentication:** Required (Bearer token)

**Response Type:** Paginated list of expenses

---

## Query Parameters

All parameters are **optional**. If no filters are provided, returns all user expenses with default pagination.

| Parameter | Type | Description | Default | Example |
|-----------|------|-------------|---------|---------|
| `categoryId` | Long | Filter by category ID | - | `3` |
| `startDate` | LocalDate | Start date (inclusive) | - | `2024-12-01` |
| `endDate` | LocalDate | End date (inclusive) | - | `2024-12-31` |
| `minAmount` | BigDecimal | Minimum amount (inclusive) | - | `10.00` |
| `maxAmount` | BigDecimal | Maximum amount (inclusive) | - | `500.00` |
| `page` | Integer | Page number (0-indexed) | `0` | `0`, `1`, `2` |
| `size` | Integer | Items per page | `10` | `10`, `20`, `50` |

---

## Response Format

```json
{
  "content": [
    {
      "id": 1,
      "amount": 50.00,
      "date": "2024-12-04",
      "description": "Grocery shopping",
      "categoryId": 3
    }
  ],
  "currentPage": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `content` | Array | List of expenses on current page |
| `currentPage` | Integer | Current page number (0-indexed) |
| `pageSize` | Integer | Number of items per page |
| `totalElements` | Long | Total number of matching expenses |
| `totalPages` | Integer | Total number of pages |
| `first` | Boolean | True if this is the first page |
| `last` | Boolean | True if this is the last page |
| `hasNext` | Boolean | True if there are more pages |
| `hasPrevious` | Boolean | True if there are previous pages |

---

## Usage Examples

### 1. Get All Expenses (Paginated)

```http
GET /api/v1/expenses/filter?page=0&size=10
```

**JavaScript:**
```javascript
const response = await fetch(
  `${BASE_URL}/expenses/filter?page=0&size=10`,
  {
    headers: { 'Authorization': `Bearer ${token}` }
  }
);
const data = await response.json();

console.log(`Total expenses: ${data.totalElements}`);
console.log(`Page ${data.currentPage + 1} of ${data.totalPages}`);
```

---

### 2. Filter by Category

```http
GET /api/v1/expenses/filter?categoryId=3&page=0&size=10
```

**JavaScript:**
```javascript
async function getExpensesByCategory(categoryId, page = 0, size = 10) {
  const url = new URL(`${BASE_URL}/expenses/filter`);
  url.searchParams.append('categoryId', categoryId);
  url.searchParams.append('page', page);
  url.searchParams.append('size', size);

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return await response.json();
}

// Usage
const groceryExpenses = await getExpensesByCategory(3);
```

---

### 3. Filter by Date Range

```http
GET /api/v1/expenses/filter?startDate=2024-12-01&endDate=2024-12-31&page=0&size=10
```

**JavaScript:**
```javascript
async function getExpensesByDateRange(startDate, endDate, page = 0, size = 10) {
  const url = new URL(`${BASE_URL}/expenses/filter`);
  url.searchParams.append('startDate', startDate);
  url.searchParams.append('endDate', endDate);
  url.searchParams.append('page', page);
  url.searchParams.append('size', size);

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return await response.json();
}

// Get this month's expenses
const now = new Date();
const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);

const monthExpenses = await getExpensesByDateRange(
  formatDateForAPI(startOfMonth),
  formatDateForAPI(endOfMonth)
);
```

---

### 4. Filter by Amount Range

```http
GET /api/v1/expenses/filter?minAmount=50&maxAmount=200&page=0&size=10
```

**JavaScript:**
```javascript
async function getExpensesByAmountRange(minAmount, maxAmount, page = 0, size = 10) {
  const url = new URL(`${BASE_URL}/expenses/filter`);
  url.searchParams.append('minAmount', minAmount);
  url.searchParams.append('maxAmount', maxAmount);
  url.searchParams.append('page', page);
  url.searchParams.append('size', size);

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return await response.json();
}

// Get expenses between $50 and $200
const midRangeExpenses = await getExpensesByAmountRange(50, 200);
```

---

### 5. Combined Filters

```http
GET /api/v1/expenses/filter?categoryId=3&startDate=2024-12-01&endDate=2024-12-31&minAmount=20&maxAmount=100&page=0&size=20
```

**JavaScript:**
```javascript
async function filterExpenses(filters) {
  const url = new URL(`${BASE_URL}/expenses/filter`);

  // Add all provided filters
  if (filters.categoryId) url.searchParams.append('categoryId', filters.categoryId);
  if (filters.startDate) url.searchParams.append('startDate', filters.startDate);
  if (filters.endDate) url.searchParams.append('endDate', filters.endDate);
  if (filters.minAmount) url.searchParams.append('minAmount', filters.minAmount);
  if (filters.maxAmount) url.searchParams.append('maxAmount', filters.maxAmount);

  url.searchParams.append('page', filters.page || 0);
  url.searchParams.append('size', filters.size || 10);

  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return await response.json();
}

// Usage
const results = await filterExpenses({
  categoryId: 3,
  startDate: '2024-12-01',
  endDate: '2024-12-31',
  minAmount: 20,
  maxAmount: 100,
  page: 0,
  size: 20
});
```

---

## Pagination Examples

### Navigate Through Pages

```javascript
class ExpensePagination {
  constructor() {
    this.currentPage = 0;
    this.pageSize = 10;
    this.filters = {};
  }

  async loadPage(page) {
    this.currentPage = page;
    const data = await filterExpenses({
      ...this.filters,
      page: this.currentPage,
      size: this.pageSize
    });

    this.displayExpenses(data.content);
    this.updatePaginationUI(data);

    return data;
  }

  async nextPage() {
    return await this.loadPage(this.currentPage + 1);
  }

  async previousPage() {
    return await this.loadPage(this.currentPage - 1);
  }

  async firstPage() {
    return await this.loadPage(0);
  }

  async lastPage(totalPages) {
    return await this.loadPage(totalPages - 1);
  }

  updatePaginationUI(data) {
    // Update page counter
    document.getElementById('pageInfo').textContent =
      `Page ${data.currentPage + 1} of ${data.totalPages}`;

    // Update button states
    document.getElementById('firstBtn').disabled = data.first;
    document.getElementById('prevBtn').disabled = !data.hasPrevious;
    document.getElementById('nextBtn').disabled = !data.hasNext;
    document.getElementById('lastBtn').disabled = data.last;

    // Update total count
    document.getElementById('totalCount').textContent =
      `Total: ${data.totalElements} expenses`;
  }

  displayExpenses(expenses) {
    // Your UI update logic here
    console.log('Displaying expenses:', expenses);
  }
}

// Usage
const pagination = new ExpensePagination();
await pagination.loadPage(0);
```

---

### Infinite Scroll

```javascript
class InfiniteScrollExpenses {
  constructor() {
    this.currentPage = 0;
    this.pageSize = 20;
    this.loading = false;
    this.hasMore = true;
    this.allExpenses = [];
  }

  async loadMore() {
    if (this.loading || !this.hasMore) return;

    this.loading = true;

    try {
      const data = await filterExpenses({
        page: this.currentPage,
        size: this.pageSize
      });

      this.allExpenses.push(...data.content);
      this.hasMore = data.hasNext;
      this.currentPage++;

      this.appendExpenses(data.content);

    } finally {
      this.loading = false;
    }
  }

  appendExpenses(expenses) {
    // Append to DOM
    const container = document.getElementById('expenses-list');
    expenses.forEach(expense => {
      const element = this.createExpenseElement(expense);
      container.appendChild(element);
    });
  }

  createExpenseElement(expense) {
    // Create and return expense DOM element
    const div = document.createElement('div');
    div.innerHTML = `
      <div class="expense-item">
        <span>${expense.description}</span>
        <span>$${expense.amount}</span>
        <span>${expense.date}</span>
      </div>
    `;
    return div;
  }
}

// Setup scroll listener
const infiniteScroll = new InfiniteScrollExpenses();

window.addEventListener('scroll', () => {
  if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
    infiniteScroll.loadMore();
  }
});

// Load first page
infiniteScroll.loadMore();
```

---

## Filter Form Component

### Complete Example

```javascript
class ExpenseFilterForm {
  constructor() {
    this.filters = {
      categoryId: null,
      startDate: null,
      endDate: null,
      minAmount: null,
      maxAmount: null,
      page: 0,
      size: 10
    };
  }

  // Set filter values from form
  setFilters(formData) {
    this.filters = {
      categoryId: formData.categoryId || null,
      startDate: formData.startDate || null,
      endDate: formData.endDate || null,
      minAmount: formData.minAmount || null,
      maxAmount: formData.maxAmount || null,
      page: 0, // Reset to first page when filters change
      size: parseInt(formData.pageSize) || 10
    };
  }

  // Clear all filters
  clearFilters() {
    this.filters = {
      categoryId: null,
      startDate: null,
      endDate: null,
      minAmount: null,
      maxAmount: null,
      page: 0,
      size: 10
    };
  }

  // Apply filters and fetch results
  async applyFilters() {
    try {
      const results = await filterExpenses(this.filters);
      this.displayResults(results);
      return results;
    } catch (error) {
      console.error('Filter failed:', error);
      alert('Failed to filter expenses');
    }
  }

  displayResults(data) {
    // Display expenses
    const container = document.getElementById('expense-results');
    container.innerHTML = '';

    if (data.content.length === 0) {
      container.innerHTML = '<p>No expenses found matching your filters.</p>';
      return;
    }

    data.content.forEach(expense => {
      const item = document.createElement('div');
      item.className = 'expense-item';
      item.innerHTML = `
        <div class="expense-info">
          <strong>${expense.description}</strong>
          <span class="amount">$${expense.amount.toFixed(2)}</span>
          <span class="date">${expense.date}</span>
        </div>
      `;
      container.appendChild(item);
    });

    // Update pagination info
    document.getElementById('page-info').textContent =
      `Showing ${data.content.length} of ${data.totalElements} expenses (Page ${data.currentPage + 1}/${data.totalPages})`;
  }
}

// HTML Form Example
/*
<form id="filter-form">
  <select name="categoryId">
    <option value="">All Categories</option>
    <option value="1">Food</option>
    <option value="2">Transport</option>
    <option value="3">Entertainment</option>
  </select>

  <input type="date" name="startDate" placeholder="Start Date">
  <input type="date" name="endDate" placeholder="End Date">

  <input type="number" name="minAmount" placeholder="Min Amount" step="0.01">
  <input type="number" name="maxAmount" placeholder="Max Amount" step="0.01">

  <select name="pageSize">
    <option value="10">10 per page</option>
    <option value="20">20 per page</option>
    <option value="50">50 per page</option>
  </select>

  <button type="submit">Apply Filters</button>
  <button type="button" id="clear-filters">Clear Filters</button>
</form>
*/

// Setup event listeners
const filterForm = new ExpenseFilterForm();

document.getElementById('filter-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(e.target);
  filterForm.setFilters(Object.fromEntries(formData));
  await filterForm.applyFilters();
});

document.getElementById('clear-filters').addEventListener('click', async () => {
  filterForm.clearFilters();
  document.getElementById('filter-form').reset();
  await filterForm.applyFilters();
});
```

---

## Common Use Cases

### 1. Monthly Expense View

```javascript
async function loadMonthlyExpenses(year, month) {
  const startDate = new Date(year, month, 1);
  const endDate = new Date(year, month + 1, 0);

  return await filterExpenses({
    startDate: formatDateForAPI(startDate),
    endDate: formatDateForAPI(endDate),
    page: 0,
    size: 50
  });
}

// Get December 2024 expenses
const decemberExpenses = await loadMonthlyExpenses(2024, 11); // Month is 0-indexed
```

### 2. Large Expenses This Week

```javascript
async function loadLargeExpensesThisWeek(threshold = 100) {
  const now = new Date();
  const dayOfWeek = now.getDay();
  const diffToMonday = (dayOfWeek === 0 ? -6 : 1) - dayOfWeek;

  const monday = new Date(now);
  monday.setDate(now.getDate() + diffToMonday);

  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);

  return await filterExpenses({
    startDate: formatDateForAPI(monday),
    endDate: formatDateForAPI(sunday),
    minAmount: threshold,
    page: 0,
    size: 20
  });
}
```

### 3. Category Expenses in Date Range

```javascript
async function loadCategoryExpensesInRange(categoryId, startDate, endDate) {
  return await filterExpenses({
    categoryId: categoryId,
    startDate: startDate,
    endDate: endDate,
    page: 0,
    size: 100
  });
}
```

### 4. Load All Matching Expenses

```javascript
async function loadAllFilteredExpenses(filters) {
  let allExpenses = [];
  let page = 0;
  let hasMore = true;

  while (hasMore) {
    const data = await filterExpenses({
      ...filters,
      page: page,
      size: 50
    });

    allExpenses.push(...data.content);
    hasMore = data.hasNext;
    page++;
  }

  return allExpenses;
}

// Usage
const allGroceryExpenses = await loadAllFilteredExpenses({
  categoryId: 3,
  startDate: '2024-01-01',
  endDate: '2024-12-31'
});
```

---

## Best Practices

### 1. Debounce Filter Changes

```javascript
function debounce(func, wait) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

const debouncedFilter = debounce(async (filters) => {
  await filterForm.applyFilters();
}, 500);

// Use on amount inputs
document.getElementById('minAmount').addEventListener('input', () => {
  debouncedFilter(filterForm.filters);
});
```

### 2. Cache Results

```javascript
const filterCache = new Map();

async function cachedFilterExpenses(filters) {
  const cacheKey = JSON.stringify(filters);

  if (filterCache.has(cacheKey)) {
    return filterCache.get(cacheKey);
  }

  const results = await filterExpenses(filters);
  filterCache.set(cacheKey, results);

  return results;
}
```

### 3. Show Loading State

```javascript
async function loadWithLoadingState() {
  const loader = document.getElementById('loader');
  const content = document.getElementById('content');

  loader.style.display = 'block';
  content.style.opacity = '0.5';

  try {
    const results = await filterExpenses(filters);
    displayResults(results);
  } finally {
    loader.style.display = 'none';
    content.style.opacity = '1';
  }
}
```

### 4. Validate Date Range

```javascript
function validateDateRange(startDate, endDate) {
  if (startDate && endDate && startDate > endDate) {
    alert('Start date must be before end date');
    return false;
  }
  return true;
}
```

---

## Validation Rules

- `minAmount` and `maxAmount` must be >= 0
- `page` must be >= 0
- `size` must be >= 1
- If both `minAmount` and `maxAmount` are provided, `minAmount` should be <= `maxAmount`
- Dates must be in ISO format (YYYY-MM-DD)

---

## Error Handling

```javascript
async function safeFilterExpenses(filters) {
  try {
    const response = await fetch(buildFilterURL(filters), {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }

    return await response.json();

  } catch (error) {
    console.error('Filter failed:', error);

    // Show user-friendly message
    if (error.message.includes('401')) {
      alert('Session expired. Please login again.');
      redirectToLogin();
    } else {
      alert('Failed to load expenses. Please try again.');
    }

    return null;
  }
}
```

---

## Performance Tips

1. **Use appropriate page sizes**: Start with 10-20 items per page
2. **Limit amount of filters**: Too many filters can slow down queries
3. **Use date ranges wisely**: Narrow date ranges perform better
4. **Cache frequent queries**: Store results for common filter combinations
5. **Debounce user input**: Wait for user to finish typing before filtering

---

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Authorize with your JWT token
3. Find `GET /api/v1/expenses/filter` endpoint
4. Click "Try it out"
5. Fill in any filter parameters
6. Click "Execute"
7. View the paginated response

---

## Quick Reference

**Basic Pagination:**
```
GET /api/v1/expenses/filter?page=0&size=10
```

**Filter by Category:**
```
GET /api/v1/expenses/filter?categoryId=3
```

**Filter by Date:**
```
GET /api/v1/expenses/filter?startDate=2024-12-01&endDate=2024-12-31
```

**Filter by Amount:**
```
GET /api/v1/expenses/filter?minAmount=50&maxAmount=200
```

**Combined:**
```
GET /api/v1/expenses/filter?categoryId=3&startDate=2024-12-01&endDate=2024-12-31&minAmount=20&maxAmount=100&page=0&size=20
```

---

**Last Updated:** December 2024
