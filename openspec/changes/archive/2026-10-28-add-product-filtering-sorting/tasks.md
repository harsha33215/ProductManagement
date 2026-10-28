# Tasks

## 1. Database-backed filtering and deterministic ordering

- [x] 1.1 Add an isolated, test-scoped H2 configuration using the existing JPA setup; verify repository integration tests run without a developer-local PostgreSQL instance.
- [x] 1.2 Extend the existing product repository query support for optional AND-combined filters; verify H2 integration tests cover case-insensitive exact category/brand matches, inclusive price bounds, and no-filter behavior.
- [x] 1.3 Pass validated sorting and pagination through the existing product service/repository path; preserve the existing page 0/size 10 defaults and `ProductPageResponse` mapping, and append `id ASC` after user criteria; verify multiple user criteria keep their precedence and tied products resolve by ascending id before pagination.

## 2. Request validation and existing error response

- [x] 2.1 Extend the existing product-list endpoint parameters and validation while reusing its current pagination defaults and response DTO; verify request-level tests cover filters, repeated sort parameters, sorting, pagination, and response compatibility.
- [x] 2.2 Reuse and extend the existing `GlobalExceptionHandler` and `ErrorResponse` convention for malformed sort parameters, unsupported fields, invalid directions, and `minPrice > maxPrice`; verify each request returns HTTP 400 with the established error response shape.

## 3. Release validation

- [x] 3.1 Run `.\mvnw.cmd test`; verify the existing suite and all new H2 repository and request-level tests pass together.
- [x] 3.2 Include PostgreSQL integration verification in the deployment validation flow; verify the filter, repeated-sort (including id tie-breaker), and pagination scenarios pass against the configured PostgreSQL environment before release.
