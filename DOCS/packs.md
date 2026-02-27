# Context Packs

## What is a Pack?

A pack is a collection of rules, instructions, path-scoped rules, and skills for a specific language or framework. Each pack provides:

- **rules.md**: Architecture boundaries, coding standards, error handling patterns
- **repo-instructions.md**: Build/test/run commands, project structure, PR expectations
- **paths/**: Path-scoped rules that apply to specific directories or files
- **skills/**: Actionable patterns for common development tasks

## Available Packs

### Java (`java`)
For Java backend services (Spring Boot, Micronaut, Quarkus).
- Architecture: hexagonal/clean architecture
- Database: Flyway/Liquibase migrations, Testcontainers
- Testing: JUnit, slice tests, integration tests
- Path rules: migrations, infrastructure code

### Flutter (`flutter`)
For Flutter mobile and web applications.
- Architecture: feature-based folder structure
- State management: Bloc/Cubit/Provider conventions
- Testing: widget tests, unit tests
- Path rules: pubspec.yaml, test directory

## Pack Configuration

In `aictx-context.yaml`, select packs:
```yaml
packs:
  - java      # or flutter
```

Each pack has sensible defaults for globals, skills, and MCP toolsets that are applied automatically.
