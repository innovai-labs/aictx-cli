---
id: flutter-feature-patterns
description: Pattern for implementing new features in a Flutter app
whenToUse: When implementing a new feature, screen, or user-facing functionality
signals:
  - "new feature"
  - "add screen"
  - "implement page"
  - "create view"
  - "add functionality"
---
# Flutter Feature Pattern

When implementing a new feature, follow this structure:

## 1. Create Feature Folder
```
lib/features/<feature_name>/
  ├── data/
  │   ├── models/          # Data models, DTOs
  │   ├── repositories/    # Repository implementations
  │   └── data_sources/    # API clients, local storage
  ├── domain/              # (optional) Entities, use cases
  └── presentation/
      ├── screens/         # Full-page widgets
      ├── widgets/         # Feature-specific widgets
      └── bloc/            # State management (or cubit/, provider/)
```

## 2. Implement Data Layer
```dart
class FeatureRepository {
  final ApiClient _apiClient;

  FeatureRepository(this._apiClient);

  Future<List<FeatureModel>> getItems() async {
    final response = await _apiClient.get('/items');
    return response.map((e) => FeatureModel.fromJson(e)).toList();
  }
}
```

## 3. Implement State Management
```dart
class FeatureCubit extends Cubit<FeatureState> {
  final FeatureRepository _repository;

  FeatureCubit(this._repository) : super(FeatureInitial());

  Future<void> loadItems() async {
    emit(FeatureLoading());
    try {
      final items = await _repository.getItems();
      emit(FeatureLoaded(items));
    } catch (e) {
      emit(FeatureError(e.toString()));
    }
  }
}
```

## 4. Build the UI
```dart
class FeatureScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<FeatureCubit, FeatureState>(
      builder: (context, state) => switch (state) {
        FeatureLoading() => const CircularProgressIndicator(),
        FeatureLoaded(:final items) => FeatureListView(items: items),
        FeatureError(:final message) => ErrorWidget(message: message),
        _ => const SizedBox.shrink(),
      },
    );
  }
}
```

## 5. Write Tests
- Unit test the repository with mocked data sources
- Unit test the Cubit/Bloc with mocked repository
- Widget test the screen with mocked state

## Checklist
- [ ] Feature folder created with proper structure
- [ ] Data models with serialization
- [ ] Repository with error handling
- [ ] State management with loading/error/success states
- [ ] UI handles all states gracefully
- [ ] Unit tests for business logic
- [ ] Widget tests for UI
- [ ] Route registered (if new screen)
- [ ] Localization for user-visible strings
