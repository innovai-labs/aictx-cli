---
applyTo: "test/**"
---
# Flutter Testing Conventions

- Mirror the `lib/` folder structure under `test/`
- Use `testWidgets()` for widget tests, `test()` for pure unit tests
- Name test files with `_test.dart` suffix
- Use `setUp()` and `tearDown()` for common test setup
- Mock dependencies using `mocktail` or `mockito`
- Test widget rendering, interactions, and state transitions
- Use `pumpWidget()` and `pumpAndSettle()` correctly; avoid arbitrary delays
- Golden tests are optional but encouraged for complex UI components
- Group related tests with `group()` blocks
- Test error states and edge cases, not just happy paths
