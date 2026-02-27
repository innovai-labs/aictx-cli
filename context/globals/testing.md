# Testing Standards

## Testing Pyramid
- **Unit tests**: fast, isolated, cover business logic and edge cases
- **Integration tests**: verify component interactions, database access, external service contracts
- **End-to-end tests**: minimal, cover critical user journeys only

## Naming Conventions
- Test names describe the scenario and expected outcome
- Use pattern: `should_[expected]_when_[condition]` or `[method]_[scenario]_[result]`
- Group related tests logically

## Determinism
- Tests must not depend on execution order
- Avoid relying on system clock, random values, or network calls without mocking
- Use fixed seeds or deterministic test data
- Clean up test state (database, files, caches) in teardown

## Flake Avoidance
- No `sleep()` or timing-based assertions; use polling or explicit waits
- Mock external services; do not call real APIs in unit tests
- Use test containers or in-memory databases for integration tests
- Retry logic belongs in production code, not tests

## Coverage
- Aim for meaningful coverage, not a percentage target
- Prioritize coverage on business-critical paths
- Untested code is a liability; dead code should be removed
