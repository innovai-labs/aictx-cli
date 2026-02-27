---
id: java-service-skeleton
description: Skeleton pattern for adding new endpoints or services to a Java backend
whenToUse: When asked to add a new REST endpoint, service class, or CRUD operation
signals:
  - "add endpoint"
  - "create service"
  - "new API"
  - "CRUD"
  - "REST resource"
---
# Java Service Skeleton

When implementing a new endpoint or service, follow this pattern:

## 1. Define the Domain Model
- Create or reuse a domain entity/record
- Keep domain objects free of framework annotations where possible

## 2. Create the Repository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

## 3. Implement the Service
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUser(Long id) {
        return userRepository.findById(id)
            .map(UserDto::from)
            .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
```

## 4. Create the Controller
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
}
```

## 5. Write Tests
- Unit test the service with mocked repository
- Slice test the controller with @WebMvcTest
- Integration test the repository with @DataJpaTest and Testcontainers

## Checklist
- [ ] Domain model defined
- [ ] Repository interface created
- [ ] Service with business logic
- [ ] Controller with validation and proper HTTP status codes
- [ ] DTOs for request/response (no entity leakage)
- [ ] Unit + integration tests
- [ ] Migration if new table/columns needed
