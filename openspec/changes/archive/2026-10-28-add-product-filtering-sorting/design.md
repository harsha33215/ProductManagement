# Design

## Context

See `proposal.md` for motivation and `specs/product-filtering-sorting/spec.md` for the external behavior contract. The current `GET /api/products` path passes page and size to `ProductService`, which uses `ProductRepository.findAll(Pageable)` and maps `Page<Product>` to the existing `ProductPageResponse`. `Product` already has category, brand, price, name, quantity, and id fields. The repository extends `JpaRepository`. `GlobalExceptionHandler` currently formats `ProductNotFoundException` as the existing `ErrorResponse`; there is no shared handler yet for invalid query parameters. The Maven test setup currently has only a context-load test and no H2 dependency or test-specific database configuration.

## Goals / Non-Goals

**Goals:**
- Preserve the specified filters, approved sort fields, pagination defaults, and existing response DTO contract.
- Apply filters, sorting, and pagination in one database-backed page query.
- Make ordering deterministic by appending `id ASC` after all user-supplied sort criteria.
- Return invalid query inputs as HTTP 400 using the existing global error response format.
- Cover repository behavior with H2 and include PostgreSQL verification in deployment validation.

**Non-Goals:**
- Change product persistence fields, CRUD operations, or database schema.
- Add substring search, filter operators beyond the specified exact and range comparisons, or sorting fields outside the allowlist.
- Change user-supplied sort precedence or introduce a different response DTO/error body contract.

## Decisions

1. **Use Spring Data JPA specifications for optional predicates.** Extend the repository query capabilities with `JpaSpecificationExecutor` and build a specification from supplied filters only. AND the predicates; compare category and brand exactly and case-insensitively; use inclusive price bounds. This reuses the existing JPA stack and supports pageable count queries. Fixed derived-query combinations or native SQL would multiply query variants or couple behavior to a database dialect.

2. **Build a pageable from validated sort criteria and append a stable tie-breaker.** Parse repeated Spring Data-compatible `sort=field,direction` values, validate each field against `name`, `category`, `price`, and `quantity`, and accept only `asc` or `desc` directions. Keep all user sort criteria in their supplied order, then append `id ASC` as the final criterion so equal user sort values have deterministic ordering. Preserve page 0 and size 10 defaults. Do not pass arbitrary property names to Spring Data.

3. **Use existing error handling for all invalid query parameters.** Reject malformed sort syntax, unsupported fields, invalid directions, and `minPrice > maxPrice` as HTTP 400. Extend the existing `GlobalExceptionHandler` to map the relevant invalid-input and request-parameter binding/conversion failures into the existing `ErrorResponse` shape. Do not introduce a parallel error DTO or allow Spring/JPA exceptions to escape as server errors.

4. **Use H2 for repository integration coverage and PostgreSQL for deployment validation.** Add only a test-scoped H2 dependency and a test configuration that reuses the project's JPA setup while isolating tests from developer-local PostgreSQL. Repository integration tests cover filtering, repeated sorting including the id tie-breaker, and pagination; request-level tests cover HTTP 400 cases and response compatibility. As a deployment validation step, run the same relevant integration checks against the configured PostgreSQL environment before release.

## Risks / Trade-offs

- [H2 can differ from PostgreSQL in dialect-specific behavior] → Keep queries in standard JPA criteria and run PostgreSQL integration verification as part of deployment validation.
- [Appending `id ASC` only resolves ties after all requested criteria] → Preserve every user criterion's order and verify equal-key records are ordered by id in tests.
- [Spring request binding failures and custom validation failures can originate at different layers] → Handle both through the existing global advice and assert the common HTTP 400 `ErrorResponse` format in request-level tests.
- [Case-insensitive exact matching may limit use of ordinary indexes] → Review query plans if measured product volume warrants follow-up indexing; do not broaden this change without evidence.

## Migration Plan

No schema migration is required. Deploy as a backward-compatible endpoint extension. Validate against PostgreSQL during deployment before release. Roll back by reverting the application change; existing clients using only page and size retain the existing response shape.
