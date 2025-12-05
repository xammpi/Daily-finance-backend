# Frontend API Guide - Search & Filter Endpoints

## Overview

This guide provides frontend developers with everything needed to implement the new generic search and filter functionality for Expenses and Categories.

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication

All search endpoints require JWT Bearer token authentication:

```javascript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

---

## Expense Search API

### Endpoint
```
POST /api/v1/expenses/search
```

### Request Body Structure

```typescript
interface FilterRequest {
  criteria: SearchCriteria[];    // Array of search conditions
  page?: number;                 // Page number (0-indexed), default: 0
  size?: number;                 // Items per page, default: 20, max: 100
  sortBy?: string;               // Field to sort by, default: "date"
  sortOrder?: "ASC" | "DESC";    // Sort direction, default: "DESC"
}

interface SearchCriteria {
  field: string;                 // Field name (e.g., "amount", "date", "category.name")
  operation: SearchOperation;    // Operation type (see below)
  value: string | number;        // Search value
  valueTo?: string | number;     // Second value (only for BETWEEN)
}

type SearchOperation =
  | "EQUALS"
  | "NOT_EQUALS"
  | "GREATER_THAN"
  | "GREATER_THAN_OR_EQUAL"
  | "LESS_THAN"
  | "LESS_THAN_OR_EQUAL"
  | "LIKE"              // Contains (case-insensitive)
  | "STARTS_WITH"       // Starts with
  | "ENDS_WITH"         // Ends with
  | "IN"                // In list (comma-separated)
  | "NOT_IN"            // Not in list
  | "IS_NULL"
  | "IS_NOT_NULL"
  | "BETWEEN";          // Range (requires valueTo)
```

### Response Structure

```typescript
interface PagedResponse<T> {
  content: T[];                  // Array of items
  currentPage: number;           // Current page (0-indexed)
  pageSize: number;              // Items per page
  totalElements: number;         // Total number of items
  totalPages: number;            // Total number of pages
  first: boolean;                // Is first page
  last: boolean;                 // Is last page
  hasNext: boolean;              // Has next page
  hasPrevious: boolean;          // Has previous page
}

interface ExpenseResponse {
  id: number;
  amount: number;
  date: string;                  // ISO date: "2024-12-01"
  description: string;
  categoryId: number;
  categoryName: string;
  createdAt: string;             // ISO datetime
  updatedAt: string;             // ISO datetime
}
```

### Available Search Fields

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| `id` | number | `"123"` | Expense ID |
| `amount` | number | `"150.50"` | Expense amount |
| `date` | date | `"2024-12-01"` | Expense date (ISO format) |
| `description` | string | `"Groceries"` | Expense description |
| `category.id` | number | `"5"` | Category ID (nested) |
| `category.name` | string | `"Food"` | Category name (nested) |
| `createdAt` | datetime | `"2024-12-01T10:30:00"` | Created timestamp |
| `updatedAt` | datetime | `"2024-12-01T10:30:00"` | Updated timestamp |

---

## Frontend Implementation Examples

### Example 1: Simple Search - Expenses Greater Than $100

```javascript
async function searchExpensesOverAmount(minAmount) {
  const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria: [
        {
          field: "amount",
          operation: "GREATER_THAN",
          value: minAmount
        }
      ],
      page: 0,
      size: 20,
      sortBy: "date",
      sortOrder: "DESC"
    })
  });

  return await response.json();
}

// Usage
const result = await searchExpensesOverAmount(100);
console.log(`Found ${result.totalElements} expenses`);
console.log(result.content); // Array of ExpenseResponse
```

### Example 2: Date Range Search

```javascript
async function searchExpensesByDateRange(startDate, endDate) {
  const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria: [
        {
          field: "date",
          operation: "BETWEEN",
          value: startDate,      // "2024-12-01"
          valueTo: endDate       // "2024-12-31"
        }
      ],
      sortBy: "date",
      sortOrder: "DESC"
    })
  });

  return await response.json();
}

// Usage
const december = await searchExpensesByDateRange("2024-12-01", "2024-12-31");
```

### Example 3: Search by Category

```javascript
async function searchExpensesByCategory(categoryName) {
  const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria: [
        {
          field: "category.name",
          operation: "EQUALS",
          value: categoryName
        }
      ],
      sortBy: "date",
      sortOrder: "DESC"
    })
  });

  return await response.json();
}

// Usage
const foodExpenses = await searchExpensesByCategory("Food");
```

### Example 4: Text Search in Description

```javascript
async function searchExpensesByDescription(searchText) {
  const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria: [
        {
          field: "description",
          operation: "LIKE",
          value: searchText
        }
      ],
      sortBy: "date",
      sortOrder: "DESC"
    })
  });

  return await response.json();
}

// Usage - finds "grocery", "Grocery Store", "GROCERY", etc.
const groceryExpenses = await searchExpensesByDescription("grocery");
```

### Example 5: Multiple Filters Combined

```javascript
async function searchExpensesAdvanced(filters) {
  const criteria = [];

  // Add amount filter
  if (filters.minAmount) {
    criteria.push({
      field: "amount",
      operation: "GREATER_THAN_OR_EQUAL",
      value: filters.minAmount
    });
  }

  if (filters.maxAmount) {
    criteria.push({
      field: "amount",
      operation: "LESS_THAN_OR_EQUAL",
      value: filters.maxAmount
    });
  }

  // Add date range filter
  if (filters.startDate && filters.endDate) {
    criteria.push({
      field: "date",
      operation: "BETWEEN",
      value: filters.startDate,
      valueTo: filters.endDate
    });
  }

  // Add category filter
  if (filters.categoryIds && filters.categoryIds.length > 0) {
    criteria.push({
      field: "category.id",
      operation: "IN",
      value: filters.categoryIds.join(',')  // "1,2,3"
    });
  }

  // Add text search
  if (filters.searchText) {
    criteria.push({
      field: "description",
      operation: "LIKE",
      value: filters.searchText
    });
  }

  const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria,
      page: filters.page || 0,
      size: filters.size || 20,
      sortBy: filters.sortBy || "date",
      sortOrder: filters.sortOrder || "DESC"
    })
  });

  return await response.json();
}

// Usage
const results = await searchExpensesAdvanced({
  minAmount: 50,
  maxAmount: 200,
  startDate: "2024-12-01",
  endDate: "2024-12-31",
  categoryIds: [1, 2, 3],
  searchText: "grocery",
  page: 0,
  size: 10
});
```

### Example 6: Pagination Implementation

```javascript
function ExpenseList() {
  const [expenses, setExpenses] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  async function loadExpenses(page = 0) {
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          criteria: [],  // Empty = all expenses
          page: page,
          size: 20,
          sortBy: "date",
          sortOrder: "DESC"
        })
      });

      const data = await response.json();
      setExpenses(data.content);
      setCurrentPage(data.currentPage);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Failed to load expenses:', error);
    } finally {
      setLoading(false);
    }
  }

  // Pagination controls
  return (
    <div>
      <ExpenseTable expenses={expenses} />

      <div className="pagination">
        <button
          onClick={() => loadExpenses(currentPage - 1)}
          disabled={currentPage === 0}
        >
          Previous
        </button>

        <span>Page {currentPage + 1} of {totalPages}</span>

        <button
          onClick={() => loadExpenses(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  );
}
```

---

## Category Search API

### Endpoint
```
POST /api/v1/categories/search
```

### Request/Response Structure
Same as Expense Search (see above)

### Available Search Fields

| Field | Type | Example | Description |
|-------|------|---------|-------------|
| `id` | number | `"5"` | Category ID |
| `name` | string | `"Food"` | Category name |
| `description` | string | `"Daily groceries"` | Category description |
| `createdAt` | datetime | `"2024-12-01T10:30:00"` | Created timestamp |
| `updatedAt` | datetime | `"2024-12-01T10:30:00"` | Updated timestamp |

### Example: Search Categories by Name

```javascript
async function searchCategories(searchText) {
  const response = await fetch('http://localhost:8080/api/v1/categories/search', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      criteria: [
        {
          field: "name",
          operation: "LIKE",
          value: searchText
        }
      ],
      sortBy: "name",
      sortOrder: "ASC"
    })
  });

  return await response.json();
}

// Usage
const categories = await searchCategories("food");
```

---

## React Hooks Example

### Custom Hook for Expense Search

```javascript
import { useState, useCallback } from 'react';

function useExpenseSearch() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false
  });

  const search = useCallback(async (criteria, page = 0, size = 20, sortBy = "date", sortOrder = "DESC") => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          criteria,
          page,
          size,
          sortBy,
          sortOrder
        })
      });

      if (!response.ok) {
        throw new Error('Search failed');
      }

      const data = await response.json();
      setExpenses(data.content);
      setPagination({
        currentPage: data.currentPage,
        pageSize: data.pageSize,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        hasNext: data.hasNext,
        hasPrevious: data.hasPrevious
      });

      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    expenses,
    loading,
    error,
    pagination,
    search
  };
}

// Usage in component
function ExpenseSearchPage() {
  const { expenses, loading, pagination, search } = useExpenseSearch();
  const [filters, setFilters] = useState({
    minAmount: '',
    maxAmount: '',
    startDate: '',
    endDate: '',
    searchText: ''
  });

  const handleSearch = async () => {
    const criteria = [];

    if (filters.minAmount) {
      criteria.push({
        field: "amount",
        operation: "GREATER_THAN_OR_EQUAL",
        value: filters.minAmount
      });
    }

    if (filters.maxAmount) {
      criteria.push({
        field: "amount",
        operation: "LESS_THAN_OR_EQUAL",
        value: filters.maxAmount
      });
    }

    if (filters.startDate && filters.endDate) {
      criteria.push({
        field: "date",
        operation: "BETWEEN",
        value: filters.startDate,
        valueTo: filters.endDate
      });
    }

    if (filters.searchText) {
      criteria.push({
        field: "description",
        operation: "LIKE",
        value: filters.searchText
      });
    }

    await search(criteria);
  };

  return (
    <div>
      {/* Filter inputs */}
      <input
        type="number"
        value={filters.minAmount}
        onChange={(e) => setFilters({...filters, minAmount: e.target.value})}
        placeholder="Min amount"
      />
      <input
        type="number"
        value={filters.maxAmount}
        onChange={(e) => setFilters({...filters, maxAmount: e.target.value})}
        placeholder="Max amount"
      />
      <input
        type="date"
        value={filters.startDate}
        onChange={(e) => setFilters({...filters, startDate: e.target.value})}
      />
      <input
        type="date"
        value={filters.endDate}
        onChange={(e) => setFilters({...filters, endDate: e.target.value})}
      />
      <input
        type="text"
        value={filters.searchText}
        onChange={(e) => setFilters({...filters, searchText: e.target.value})}
        placeholder="Search description"
      />
      <button onClick={handleSearch}>Search</button>

      {/* Results */}
      {loading && <p>Loading...</p>}
      {expenses.map(expense => (
        <div key={expense.id}>
          <h3>{expense.description}</h3>
          <p>${expense.amount} - {expense.date}</p>
          <p>Category: {expense.categoryName}</p>
        </div>
      ))}

      {/* Pagination */}
      <p>
        Showing {expenses.length} of {pagination.totalElements} results
      </p>
    </div>
  );
}
```

---

## Common Use Cases

### 1. Filter by This Month's Expenses

```javascript
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);

const thisMonth = await searchExpensesByDateRange(
  firstDay.toISOString().split('T')[0],
  lastDay.toISOString().split('T')[0]
);
```

### 2. Filter by Multiple Categories

```javascript
const categoryIds = [1, 2, 3]; // Food, Transport, Shopping

const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${getToken()}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    criteria: [
      {
        field: "category.id",
        operation: "IN",
        value: categoryIds.join(',')
      }
    ]
  })
});
```

### 3. Find All Expenses Without a Category

```javascript
const uncategorized = await fetch('http://localhost:8080/api/v1/expenses/search', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${getToken()}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    criteria: [
      {
        field: "category.id",
        operation: "IS_NULL"
      }
    ]
  })
});
```

### 4. Sort Expenses by Amount (Highest First)

```javascript
const highestExpenses = await fetch('http://localhost:8080/api/v1/expenses/search', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${getToken()}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    criteria: [],  // No filters, just sorting
    sortBy: "amount",
    sortOrder: "DESC",
    size: 10  // Top 10
  })
});
```

---

## Error Handling

### Common Error Responses

```typescript
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
```

### Example Error Handling

```javascript
async function searchWithErrorHandling(criteria) {
  try {
    const response = await fetch('http://localhost:8080/api/v1/expenses/search', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ criteria })
    });

    if (!response.ok) {
      const error = await response.json();

      if (response.status === 401) {
        // Unauthorized - redirect to login
        window.location.href = '/login';
        return;
      }

      if (response.status === 400) {
        // Bad request - show validation errors
        console.error('Validation errors:', error.details);
        alert(error.message);
        return;
      }

      throw new Error(error.message || 'Search failed');
    }

    return await response.json();
  } catch (error) {
    console.error('Search error:', error);
    throw error;
  }
}
```

---

## Performance Tips

1. **Use pagination** - Don't load all results at once
2. **Debounce search input** - Wait for user to stop typing before searching
3. **Cache results** - Store recent searches in state/localStorage
4. **Show loading states** - Improve perceived performance
5. **Limit page size** - Max 100 items per page

### Debounce Example

```javascript
import { useState, useEffect } from 'react';

function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}

// Usage
function SearchInput() {
  const [searchText, setSearchText] = useState('');
  const debouncedSearch = useDebounce(searchText, 500); // 500ms delay

  useEffect(() => {
    if (debouncedSearch) {
      performSearch(debouncedSearch);
    }
  }, [debouncedSearch]);

  return (
    <input
      value={searchText}
      onChange={(e) => setSearchText(e.target.value)}
      placeholder="Search expenses..."
    />
  );
}
```

---

## Testing with Swagger UI

You can test these endpoints in Swagger UI:

1. Start the backend: `./mvnw spring-boot:run`
2. Open: http://localhost:8080/swagger-ui.html
3. Navigate to **Expenses** or **Categories** section
4. Try the `POST /search` endpoint
5. Click "Try it out"
6. Enter your request JSON
7. Click "Execute"

---

## Quick Reference

### Most Common Searches

```javascript
// All expenses (paginated)
{ criteria: [] }

// This month's expenses
{
  criteria: [{
    field: "date",
    operation: "BETWEEN",
    value: "2024-12-01",
    valueTo: "2024-12-31"
  }]
}

// Expenses by category
{
  criteria: [{
    field: "category.name",
    operation: "EQUALS",
    value: "Food"
  }]
}

// Expenses over $100
{
  criteria: [{
    field: "amount",
    operation: "GREATER_THAN",
    value: "100"
  }]
}

// Search in description
{
  criteria: [{
    field: "description",
    operation: "LIKE",
    value: "grocery"
  }]
}
```

---

## Need Help?

- Check the comprehensive guide: `GENERIC_SEARCH_GUIDE.md`
- Test in Swagger UI: http://localhost:8080/swagger-ui.html
- Check backend logs for errors
- Verify JWT token is valid

## Summary

✅ Two main endpoints: `/expenses/search` and `/categories/search`
✅ 14 search operations (EQUALS, LIKE, BETWEEN, etc.)
✅ Supports nested fields (`category.name`)
✅ Full pagination support
✅ Flexible sorting
✅ Multiple filters with AND logic
✅ Type-safe with automatic conversion

Happy coding! 🚀
