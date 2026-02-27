# Flutter App Rules

## Architecture
- Separate UI, business logic, and data layers clearly
- Use a consistent state management approach across the app (Bloc, Riverpod, or Provider — pick one)
- Widgets should be small and composable; extract reusable widgets into separate files
- Keep build methods lean; extract complex widget trees into named methods or sub-widgets

## State Management
- Never put business logic directly in widgets
- State classes should be immutable
- Handle loading, error, and empty states explicitly in the UI
- Dispose controllers and subscriptions in dispose() or equivalent lifecycle method

## Folder Structure
```
lib/
  ├── app/           # App-level config, routing, themes
  ├── features/      # Feature modules (one folder per feature)
  │   └── <feature>/
  │       ├── data/       # Repositories, data sources, models
  │       ├── domain/     # Entities, use cases (if applicable)
  │       └── presentation/ # Widgets, screens, state management
  ├── shared/        # Shared widgets, utilities, extensions
  └── main.dart
```

## Error Handling
- Wrap async operations in try/catch at the boundary (Bloc, controller)
- Show user-friendly error messages; log technical details
- Use Result/Either types or sealed classes for operation outcomes
- Never let unhandled exceptions crash the app silently

## Localization
- Use `intl` or `flutter_localizations` for all user-visible strings
- Never hardcode display strings in widgets
- Keep translation keys descriptive and namespaced by feature

## Linting
- Follow `flutter_lints` or a stricter custom analysis_options.yaml
- Run `dart analyze` before committing; zero warnings policy
- Run `dart format .` to ensure consistent formatting

## Performance
- Use `const` constructors wherever possible
- Avoid unnecessary rebuilds; use `Selector`, `BlocSelector`, or equivalent
- Lazy-load heavy resources and screens
- Profile with DevTools before optimizing prematurely
