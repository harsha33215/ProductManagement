## Why

The ProductManagement application currently supports product CRUD operations and pagination, but product listings cannot be narrowed or ordered by product attributes. Adding filtering and sorting to the existing list endpoint will help clients locate products while retaining the current paginated response.

## What Changes

Extend the existing `GET /api/products` endpoint with optional filters and Spring Data-compatible sorting.

- Add optional `category`, `brand`, `minPrice`, and `maxPrice` filters. Combine supplied filters with AND; match category and brand exactly, ignoring case; treat price bounds as inclusive. Return HTTP 400 when `minPrice` exceeds `maxPrice`.
- Accept one or more repeated `sort=field,direction` parameters. Allow only the approved sortable fields name, category, price, and quantity, with `asc` or `desc` directions; reject unsupported fields or invalid directions with HTTP 400.
- Apply filtering, sorting, and pagination in the database query. Preserve the existing pagination defaults (page 0, size 10), response DTO structure, and behavior for requests without filters or sorting.
- Add tests for filtering combinations, sorting, pagination, and invalid query values.

## Capabilities

### New Capabilities
- `product-filtering-sorting`: Filter and sort product listings while preserving pagination and the existing response contract.

### Modified Capabilities
None.

## Impact

The `GET /api/products` API, its product listing query path, and related tests. The product response DTO and persistence schema remain unchanged.
