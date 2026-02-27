# Java Service Rules

## Architecture
- Follow hexagonal / clean architecture: separate domain, application, and infrastructure layers
- Domain logic must not depend on framework annotations or infrastructure concerns
- Use dependency injection; prefer constructor injection over field injection
- Keep controllers thin: delegate to service layer immediately

## Error Handling
- Use domain-specific exceptions, not generic RuntimeException
- Map exceptions to appropriate HTTP status codes in a centralized handler
- Always include correlation/request IDs in error responses
- Log exceptions at the boundary where they are handled, not where they are thrown
- Never swallow exceptions silently

## Dependencies
- Pin dependency versions explicitly (no dynamic ranges)
- Use a BOM (Bill of Materials) for framework dependency alignment
- Minimize transitive dependencies; exclude unused transitives
- Justify new dependencies in PR description

## Code Style
- Follow project formatter configuration (Checkstyle/Spotless)
- Prefer immutable objects and records where applicable
- Use Optional for return types, never for parameters
- Avoid null; use Optional or throw early
- Keep methods under 30 lines; extract complex logic into named methods

## Database
- Use migrations for all schema changes (Flyway or Liquibase)
- Never modify existing migrations; always create new ones
- Use connection pooling (HikariCP)
- Parameterize all queries; never concatenate user input into SQL

## Testing
- Unit test business logic in isolation (mock infrastructure)
- Integration test repositories against a real database (Testcontainers)
- Use slice tests for controllers (@WebMvcTest) and repositories (@DataJpaTest)
- Name tests descriptively: `should_returnNotFound_when_userDoesNotExist`
