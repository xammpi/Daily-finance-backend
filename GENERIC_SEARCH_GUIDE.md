# Generic Search & Filter System

## Overview

This application implements a **generic, reusable search and filter framework** that works with any JPA entity. The framework provides dynamic query building with support for multiple operations, pagination, and sorting.

## Architecture

### Core Components

1. **Generic DTOs** (`com.expensetracker.dto.common`)
   - `SearchOperation` - Enum of available filter operations
   - `SearchCriteria` - Single search criterion
   - `FilterRequest` - Complete filter request with pagination
   - `PageRequest` / `PagedResponse` - Pagination wrappers
   - `SortOrder` - Sorting direction (ASC/DESC)

2. **Generic Specification Builder** (`com.expensetracker.specification`)
   - `GenericSpecification<T>` - Converts SearchCriteria to JPA Specification
   - `SpecificationBuilder` - Combines multiple specifications with AND/OR logic

3. **Repository Integration**
   - All repositories extend `JpaSpecificationExecutor<T>`
   - Enables dynamic query execution with specifications

4. **Service Layer**
   - Each service implements `search{Entity}` method using the generic framework
   - Automatically filters by current user
   - Supports custom defaults (e.g., sort order)

5. **Controller Layer**
   - POST `/api/v1/{entities}/search` endpoints
   - Accepts `FilterRequest` JSON body
   - Returns paginated results

## Supported Operations

| Operation | Description | Example |
|-----------|-------------|---------|
| `EQUALS` | Exact match | `field = value` |
| `NOT_EQUALS` | Not equal | `field != value` |
| `GREATER_THAN` | Greater than | `field > value` |
| `GREATER_THAN_OR_EQUAL` | Greater or equal | `field >= value` |
| `LESS_THAN` | Less than | `field < value` |
| `LESS_THAN_OR_EQUAL` | Less or equal | `field <= value` |
| `LIKE` | Contains (case-insensitive) | `field LIKE %value%` |
| `STARTS_WITH` | Starts with | `field LIKE value%` |
| `ENDS_WITH` | Ends with | `field LIKE %value` |
| `IN` | In list | `field IN (val1, val2, ...)` |
| `NOT_IN` | Not in list | `field NOT IN (...)` |
| `IS_NULL` | Is null | `field IS NULL` |
| `IS_NOT_NULL` | Is not null | `field IS NOT NULL` |
| `BETWEEN` | Range | `field BETWEEN val1 AND val2` |

## Usage Examples

### 1. Simple Search (Expenses > 100)

**Request:**
```bash
POST /api/v1/expenses/search
Content-Type: application/json
Authorization: Bearer <your-jwt-token>

{
  "criteria": [
    {
      "field": "amount",
      "operation": "GREATER_THAN",
      "value": "100"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "date",
  "sortOrder": "DESC"
}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "amount": 150.00,
      "date": "2024-12-01",
      "description": "Groceries",
      "categoryId": 5,
      "categoryName": "Food"
    }
  ],
  "currentPage": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "hasNext": false,
  "hasPrevious": false
}
```

### 2. Complex Multi-Criteria Search

**Search for:** Expenses between $50-$200 in December 2024 with "grocery" in description

```bash
POST /api/v1/expenses/search
{
  "criteria": [
    {
      "field": "amount",
      "operation": "BETWEEN",
      "value": "50",
      "valueTo": "200"
    },
    {
      "field": "date",
      "operation": "BETWEEN",
      "value": "2024-12-01",
      "valueTo": "2024-12-31"
    },
    {
      "field": "description",
      "operation": "LIKE",
      "value": "grocery"
    }
  ],
  "page": 0,
  "size": 10,
  "sortBy": "amount",
  "sortOrder": "DESC"
}
```

### 3. Nested Field Search

**Search by category name:**
```json
{
  "criteria": [
    {
      "field": "category.name",
      "operation": "EQUALS",
      "value": "Food"
    }
  ],
  "sortBy": "date",
  "sortOrder": "DESC"
}
```

### 4. Search with IN Operation

**Multiple category IDs:**
```json
{
  "criteria": [
    {
      "field": "category.id",
      "operation": "IN",
      "value": "1,2,3"
    }
  ]
}
```

### 5. Category Search Example

**Search categories containing "food":**
```bash
POST /api/v1/categories/search
{
  "criteria": [
    {
      "field": "name",
      "operation": "LIKE",
      "value": "food"
    }
  ],
  "page": 0,
  "size": 20,
  "sortBy": "name",
  "sortOrder": "ASC"
}
```

## Available Endpoints

### Expenses
- **POST** `/api/v1/expenses/search` - Generic search with dynamic criteria
- **GET** `/api/v1/expenses/filter` - Legacy filter (specific fields only)

### Categories
- **POST** `/api/v1/categories/search` - Generic search with dynamic criteria

## Type Conversion

The framework automatically converts string values to the target field type:

| Field Type | Example Value |
|------------|---------------|
| String | `"text"` |
| Long | `"123"` |
| Integer | `"42"` |
| Double | `"99.99"` |
| BigDecimal | `"150.50"` |
| Boolean | `"true"` |
| LocalDate | `"2024-12-01"` |
| LocalDateTime | `"2024-12-01T10:30:00"` |
| Enum | `"ENUM_VALUE"` |

## Pagination

All search endpoints support pagination:

```json
{
  "page": 0,        // 0-indexed page number
  "size": 20        // Items per page (max: 100)
}
```

## Sorting

Sort by any field in ascending or descending order:

```json
{
  "sortBy": "amount",     // Field name
  "sortOrder": "DESC"     // ASC or DESC
}
```

**Default sorting:**
- **Expenses:** `date DESC` (newest first)
- **Categories:** `name ASC` (alphabetical)

## Adding Search to New Entities

To add generic search to a new entity:

### 1. Update Repository
```java
public interface MyEntityRepository extends
    JpaRepository<MyEntity, Long>,
    JpaSpecificationExecutor<MyEntity> {  // Add this
    // ...
}
```

### 2. Add Service Method
```java
@Transactional(readOnly = true)
public PagedResponse<MyEntityResponse> searchMyEntities(FilterRequest filterRequest) {
    Long userId = getCurrentUserId();

    // Build specification
    Specification<MyEntity> spec = SpecificationBuilder.build(filterRequest);

    // Add user filter
    Specification<MyEntity> userSpec = (root, query, cb) ->
        cb.equal(root.get("user").get("id"), userId);
    spec = spec == null ? userSpec : spec.and(userSpec);

    // Create pageable with defaults
    Pageable pageable = filterRequest.toPageRequest().toSpringPageRequest();
    if (filterRequest.getSortBy() == null) {
        pageable = PageRequest.of(
            filterRequest.getPage(),
            filterRequest.getSize(),
            Sort.by(Sort.Direction.ASC, "name")  // Default sort
        );
    }

    // Execute query
    Page<MyEntity> page = myEntityRepository.findAll(spec, pageable);

    // Map to DTOs
    List<MyEntityResponse> responses = page.getContent()
        .stream()
        .map(myEntityMapper::toResponse)
        .collect(Collectors.toList());

    return PagedResponse.of(
        responses,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements()
    );
}
```

### 3. Add Controller Endpoint
```java
@PostMapping("/search")
@Operation(summary = "Advanced search with dynamic criteria")
public ResponseEntity<PagedResponse<MyEntityResponse>> searchMyEntities(
        @Valid @RequestBody FilterRequest filterRequest) {
    return ResponseEntity.ok(myEntityService.searchMyEntities(filterRequest));
}
```

## Best Practices

1. **Always filter by user:** Ensure multi-tenant data isolation
2. **Set reasonable page size limits:** Default max is 100
3. **Provide default sorting:** Better UX than random order
4. **Use appropriate operations:**
   - `LIKE` for text search
   - `BETWEEN` for date ranges
   - `EQUALS` for exact matches
5. **Validate date ranges:** Ensure startDate <= endDate in business logic
6. **Handle empty criteria:** Return all results (filtered by user)

## Security Considerations

- All search endpoints require authentication (JWT token)
- User filter is automatically applied (multi-tenant isolation)
- Input validation via Jakarta Validation annotations
- No SQL injection risk (uses JPA Criteria API)
- Field access controlled by entity structure

## Performance Tips

1. **Add database indexes** on frequently searched fields:
   ```sql
   CREATE INDEX idx_expenses_user_date ON expenses(user_id, date);
   CREATE INDEX idx_expenses_amount ON expenses(amount);
   ```

2. **Use smaller page sizes** for better response times

3. **Limit nested field depth** to avoid N+1 queries

4. **Consider caching** for frequently accessed, rarely changing data

## Troubleshooting

### Common Errors

**"Cannot cast value 'abc' to type Integer"**
- Ensure value matches field type
- Example: Use `"123"` not `"abc"` for integer fields

**"Unsupported operation: XYZ"**
- Check SearchOperation enum for supported operations
- Verify operation name spelling

**"BETWEEN operation requires both value and valueTo"**
- Provide both `value` and `valueTo` for BETWEEN queries

**Empty results**
- Verify field names match entity fields (case-sensitive)
- Check user owns the data
- Confirm search criteria are not too restrictive

## Future Enhancements

Potential additions to this framework:

- [ ] OR logic support (currently only AND)
- [ ] Custom validators per field type
- [ ] Query result caching
- [ ] Search history/saved searches
- [ ] Aggregation functions (SUM, AVG, COUNT)
- [ ] Full-text search integration
- [ ] Export to CSV/Excel with filters applied

## Testing

Test the search functionality:

```bash
# Start the application
./mvnw spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui.html

# Navigate to Expenses or Categories
# Try the POST /search endpoint
```

## Summary

This generic search framework provides:
- ✅ Works with **any entity** (Expense, Category, future entities)
- ✅ **14 search operations** (EQUALS, LIKE, BETWEEN, etc.)
- ✅ **Multiple criteria** with AND logic
- ✅ **Nested field support** (e.g., category.name)
- ✅ **Pagination** (configurable page size)
- ✅ **Sorting** (any field, ASC/DESC)
- ✅ **Type-safe** (automatic type conversion)
- ✅ **Secure** (user isolation, no SQL injection)
- ✅ **Extensible** (easy to add new entities)

The framework eliminates the need to write custom filter logic for each entity, providing a consistent, powerful search API across the entire application.
