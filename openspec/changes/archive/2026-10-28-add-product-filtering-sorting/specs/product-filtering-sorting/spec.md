# Spec Delta

## Purpose

Allow clients to find and order product listings through optional filters and sorting, without changing the existing paginated response contract.

## ADDED Requirements

### Requirement: Filter product listings
The product listing endpoint SHALL accept optional category, brand, minPrice, and maxPrice filters and combine supplied filters with AND. Category and brand matching SHALL be exact and case-insensitive, and price bounds SHALL be inclusive.

#### Scenario: Apply multiple filters together
- **WHEN** a client supplies category, brand, minPrice, and maxPrice filters
- **THEN** the listing contains only products matching every supplied filter, including products priced at either bound

#### Scenario: Apply a single optional filter
- **WHEN** a client supplies any one supported filter
- **THEN** the listing contains products matching that filter without requiring values for the other filters

### Requirement: Reject an inverted price range
The product listing endpoint SHALL reject a request when minPrice is greater than maxPrice.

#### Scenario: Minimum price exceeds maximum price
- **WHEN** a client supplies minPrice greater than maxPrice
- **THEN** the endpoint returns HTTP 400 Bad Request

### Requirement: Sort product listings
The product listing endpoint SHALL accept repeated `sort=field,direction` parameters, support the sortable fields name, category, price, and quantity, and accept asc or desc directions without regard to case.

#### Scenario: Sort by one field
- **WHEN** a client supplies `sort=price,desc`
- **THEN** products are ordered by price descending before pagination is applied

#### Scenario: Apply multiple sort criteria
- **WHEN** a client supplies `sort=category,asc&sort=price,desc`
- **THEN** products are ordered by the supplied criteria in order

#### Scenario: Reject an unsupported sort field
- **WHEN** a client supplies a sort field other than name, category, price, or quantity
- **THEN** the endpoint returns HTTP 400 Bad Request

#### Scenario: Reject an invalid sort direction
- **WHEN** a client supplies a direction other than asc or desc
- **THEN** the endpoint returns HTTP 400 Bad Request

### Requirement: Preserve product pagination and response compatibility
The product listing endpoint SHALL preserve its existing page and size parameters, defaults, and response DTO structure when filters or sorting are used or omitted.

#### Scenario: List products without filters or sorting
- **WHEN** a client requests the product listing without filters or sorting and omits page and size
- **THEN** the endpoint returns the existing response structure using page 0 and size 10

#### Scenario: Paginate a filtered and sorted listing
- **WHEN** a client requests a filtered, sorted listing with page and size values
- **THEN** the endpoint returns the requested page of the filtered and sorted products in the existing response structure
