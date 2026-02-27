# Skills

## What is a Skill?

A skill is a structured, actionable pattern that AI coding assistants can follow when performing specific tasks. Skills include step-by-step instructions, code templates, and checklists.

## Available Skills

### pr-review-checklist (Global)
- **When to use**: When reviewing pull requests or code changes
- **Provides**: Structured checklist covering correctness, security, testing, quality, and operations

### java-service-skeleton (Java Pack)
- **When to use**: When adding new REST endpoints, services, or CRUD operations
- **Provides**: Step-by-step pattern from domain model → repository → service → controller → tests

### flutter-feature-patterns (Flutter Pack)
- **When to use**: When implementing new features, screens, or user-facing functionality
- **Provides**: Feature folder structure, data/domain/presentation layers, state management pattern

## How Skills Are Used

Skills are listed in the generated `AGENTS.md` file with their "when to use" descriptions. AI coding assistants read these and apply the appropriate skill when the situation matches.

## Creating Custom Skills

See [context-authoring.md](context-authoring.md) for instructions on creating new skills.
