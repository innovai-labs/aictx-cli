# Flutter App Repository Instructions

## Build & Run
- Get dependencies: `flutter pub get`
- Run: `flutter run`
- Test: `flutter test`
- Analyze: `dart analyze`
- Format: `dart format .`
- Build release: `flutter build apk` / `flutter build ios` / `flutter build web`

## Project Structure
```
lib/
  ├── app/             # App config, routing, themes
  ├── features/        # Feature modules
  ├── shared/          # Shared utilities and widgets
  └── main.dart
test/
  ├── features/        # Tests mirroring lib/features
  ├── shared/          # Shared test utilities
  └── helpers/         # Test helpers, mocks
```

## Adding a New Feature
1. Create a folder under `lib/features/<feature_name>/`
2. Add `data/`, `domain/` (optional), and `presentation/` subfolders
3. Implement the data layer (repository, models, data sources)
4. Implement state management (Bloc/Cubit/Provider)
5. Build the UI widgets and screens
6. Register routes if the feature has its own screen
7. Write widget tests and unit tests for business logic

## PR Expectations
- All tests pass (`flutter test`)
- No analyzer warnings (`dart analyze`)
- Code is formatted (`dart format .`)
- New features include widget and unit tests
- User-visible strings use localization
- Screenshots or recordings for UI changes
