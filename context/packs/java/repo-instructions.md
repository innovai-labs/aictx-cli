# Java Service Repository Instructions

## Build & Run
- Build: `./gradlew build` (or `./mvnw package`)
- Run: `./gradlew bootRun` (or `./mvnw spring-boot:run`)
- Test: `./gradlew test`
- Lint/Format: `./gradlew spotlessCheck` / `./gradlew spotlessApply`

## Project Structure
```
src/main/java/<package>/
  ├── controller/    # REST controllers (thin, delegates to service)
  ├── service/       # Business logic
  ├── domain/        # Domain models, value objects
  ├── repository/    # Data access interfaces
  ├── config/        # Configuration classes
  └── exception/     # Custom exceptions and handlers
src/main/resources/
  ├── application.yaml
  └── db/migration/  # Flyway/Liquibase migrations
src/test/java/<package>/
  ├── controller/    # Controller slice tests
  ├── service/       # Unit tests
  └── repository/    # Integration tests
```

## Adding a New Endpoint
1. Define request/response DTOs in the controller package
2. Create or update the service interface and implementation
3. Add the controller method with proper annotations and validation
4. Write unit tests for the service and a slice test for the controller
5. Add integration tests if the endpoint involves database access
6. Update API documentation (OpenAPI/Swagger annotations)

## PR Expectations
- All tests pass
- Code formatted with project formatter
- New endpoints include tests and API docs
- Database changes use migrations
- No secrets or environment-specific values hardcoded
