# CQRS & Hexagonal Architecture Enforcement

## Overview

This document details how the survey-command service enforces **Command Query Responsibility Segregation (CQRS)** boundaries and **Hexagonal Architecture (Ports & Adapters)** principles.

## CQRS Principles Enforced

### Command Side (Write) - IMPLEMENTED ✅

**Commands** modify system state. This service implements the command side:

```
Client Request
    ↓
REST Controller (SurveyController)
    ↓ Translates HTTP → Command
Command (CreateSurveyCommand)
    ↓
Application Service (CreateSurveyService)
    ↓ Orchestrates domain logic
Domain Model (Survey aggregate)
    ↓ Business invariants enforced
Repository (SurveyRepository)
    ↓ Persists state change
Database/Store
```

### Query Side (Read) - NOT IMPLEMENTED ❌

**Queries** retrieve data without modifying state. 

> **IMPORTANT**: The survey-command service does NOT include query functionality. This enforces separation of concerns.
> 
> For reading surveys, create a separate `survey-query` service with its own read model and repository.

### Boundary Enforcement

| Component | Allowed Operations | Constraint |
|-----------|-------------------|-----------|
| Controller | POST, PUT, DELETE only | No GET for data retrieval |
| Inbound Port | Command execution | Only `CreateSurveyUseCase` |
| Application Service | Execute commands | No query methods |
| Domain Aggregate | Command methods | Only state-changing operations |
| Repository Port | `save()` only | No `findById()`, no queries |
| Adapter | Write to storage | No read operations |

## Hexagonal Architecture Boundaries

### Inbound Adapters (HTTP)

**Location**: `adapter.in.rest.SurveyController`

```java
@PostMapping
public ResponseEntity<CreateSurveyResponse> createSurvey(...) {
    // Translates HTTP → Domain Command
    CreateSurveyCommand command = new CreateSurveyCommand(...);
    // Delegates to use case port
    CreateSurveyResponse response = createSurveyUseCase.execute(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Responsibility**: HTTP protocol handling only. No business logic.

### Inbound Ports (Domain Contract)

**Location**: `domain.port.in.CreateSurveyUseCase`

```java
public interface CreateSurveyUseCase {
    CreateSurveyResponse execute(CreateSurveyCommand command);
    
    // Immutable command with validation
    class CreateSurveyCommand { ... }
    
    // Response DTO with minimal data
    class CreateSurveyResponse { ... }
}
```

**Responsibility**: Define WHAT commands the domain accepts.

### Domain Layer (Core Business)

**Location**: `domain.model.*`

```java
public class Survey {
    // Private constructor - prevents direct instantiation
    private Survey(...) { ... }
    
    // Factory method - enforces invariants at creation
    public static Survey create(String title, String description, String createdBy) {
        validateTitle(title);
        validateDescription(description);
        validateCreatedBy(createdBy);
        return new Survey(...);
    }
    
    // Command method - enforces invariants
    public void addQuestion(Question question) {
        if (this.status != SurveyStatus.DRAFT) {
            throw new IllegalStateException(...);
        }
        this.questions.add(question);
    }
    
    // NO GETTER for questions that returns mutable list
    // Only immutable copy
    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }
}
```

**Responsibility**: Pure business logic with invariant enforcement. NO Spring annotations.

### Application Layer (Orchestration)

**Location**: `application.service.CreateSurveyService`

```java
@Override
public CreateSurveyResponse execute(CreateSurveyCommand command) {
    // 1. Validate command
    if (command == null) throw new IllegalArgumentException(...);
    
    // 2. Create domain aggregate (domain enforces invariants)
    Survey survey = Survey.create(...);
    
    // 3. Persist via port (outbound)
    surveyRepository.save(survey);
    
    // 4. Return response (minimal data)
    return new CreateSurveyResponse(...);
}
```

**Responsibility**: Orchestrate domain operations without domain logic.

### Outbound Ports (Persistence Contract)

**Location**: `domain.port.out.SurveyRepository`

```java
public interface SurveyRepository {
    // ONLY WRITE operations
    void save(Survey survey);
    
    // NO read operations - intentionally omitted
    // (no findById, no findAll, no queries)
}
```

**Responsibility**: Define HOW state is persisted. Command-side only.

### Outbound Adapters (Storage)

**Location**: `adapter.out.persistence.InMemorySurveyRepository`

```java
@Override
public void save(Survey survey) {
    if (survey == null) throw new IllegalArgumentException(...);
    surveys.put(survey.getId(), survey);
}
```

**Responsibility**: Concrete persistence mechanism. Replaceable without changing domain.

## Invariant Enforcement

### Domain Invariants (Survey Aggregate)

These are checked in the domain model and CANNOT be violated:

1. **Title**: Must be 3-255 characters
   ```java
   Survey.create("ab", ...) // ❌ Throws IllegalArgumentException
   Survey.create("Valid Title", ...) // ✅
   ```

2. **Description**: Must be 10+ characters
   ```java
   Survey.create("Title", "short") // ❌ Throws IllegalArgumentException
   Survey.create("Title", "Valid description") // ✅
   ```

3. **Questions only in DRAFT**: Can't modify published surveys
   ```java
   survey.publish();
   survey.addQuestion(...) // ❌ Throws IllegalStateException
   ```

4. **Must have questions to publish**: Can't publish empty survey
   ```java
   survey.publish() // ❌ Throws IllegalStateException if no questions
   ```

### Command-Level Validation

Commands validate immediately at construction (fail-fast):

```java
new CreateSurveyCommand(null, "desc", "user") 
// ❌ Throws: "Command: title cannot be null or empty"
```

### Application Service Validation

Application service validates before delegating to domain:

```java
CreateSurveyService.execute(null) 
// ❌ Throws: "CreateSurveyCommand cannot be null"
```

## Port Boundaries

### Inbound Port Boundary

```
╔═══════════════════════════════════════════════╗
║                   DOMAIN                      ║
║                                               ║
║    Inbound Port: CreateSurveyUseCase          ║
║    ↑                                           ║
║    │ (Commands only)                          ║
║    │                                           ║
║    Application Service (Orchestrator)         ║
║    Domain Model (Survey aggregate)            ║
║                                               ║
╚═══════════════════════════════════════════════╝
↑
│ Must be implemented by application service
```

### Outbound Port Boundary

```
╔═══════════════════════════════════════════════╗
║                   DOMAIN                      ║
║                                               ║
║    Outbound Port: SurveyRepository            ║
║    (save method only)                         ║
║    ↓                                           ║
║    Persistence Adapter                        ║
║    In-Memory / JPA / MongoDB / etc            ║
║                                               ║
╚═══════════════════════════════════════════════╝
↓
│ Concrete implementation of repository
```

## CQRS Separation Example

### ❌ WRONG - Violates CQRS

```java
@GetMapping("/{id}")
public Survey findSurvey(@PathVariable String id) {
    return surveyRepository.findById(id); // ❌ QUERY in command service
}
```

**Why**: Queries should not be in command service. This violates CQRS boundary.

### ✅ RIGHT - CQRS Compliant

**survey-command service**: Only POST/PUT/DELETE
```java
@PostMapping
public ResponseEntity<CreateSurveyResponse> createSurvey(...) {
    // Only writes
}
```

**survey-query service**: Only GET (separate service)
```java
@GetMapping("/{id}")
public SurveyReadModel findSurvey(@PathVariable String id) {
    return surveyQueryRepository.findById(id); // ✅ Query in query service
}
```

## Testing Strategy

### Unit Testing (No Spring)

```java
public class SurveyTest {
    @Test
    void testSurveyInvariants() {
        // Domain has no Spring dependencies - pure unit test
        Survey survey = Survey.create("Title", "Description", "user1");
        
        survey.addQuestion(new Question(...));
        assertEquals(1, survey.getQuestions().size());
        
        survey.publish(); // Should succeed
        
        survey.addQuestion(...); // ❌ Should throw after publish
    }
}
```

### Integration Testing (Commands Only)

```java
@SpringBootTest
public class CreateSurveyCommandTest {
    @Autowired
    private CreateSurveyUseCase useCase;
    
    @Test
    void testCreateSurveyCommand() {
        CreateSurveyCommand cmd = new CreateSurveyCommand(
            "Test Survey", "Test Description", "user123"
        );
        
        CreateSurveyResponse response = useCase.execute(cmd);
        assertNotNull(response.getSurveyId());
        assertEquals("DRAFT", response.getStatus());
    }
}
```

### Controller Testing (No Queries)

```java
@SpringBootTest
public class SurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateSurveyEndpoint() throws Exception {
        mockMvc.perform(post("/protected/survey")
            .header("X-User-Id", "user123")
            .contentType(APPLICATION_JSON)
            .content("{\"title\": \"Test\", \"description\": \"Valid Description\"}"))
            .andExpect(status().isCreated());
    }
    
    @Test
    void testNOQueryEndpoints() {
        // Verify no GET endpoints for data retrieval
        // Only /ping is GET (health check)
    }
}
```

## Migration Path for Queries

When you need to read surveys:

1. **Create survey-query service** (separate microservice)
   ```
   com.asmas.surveyquery
   ├─ domain
   │  └─ model
   │     └─ SurveyReadModel (different from command aggregate)
   ├─ adapter
   │  └─ in
   │     └─ rest
   │        └─ SurveyQueryController (GET endpoints only)
   └─ config
      └─ QueryConfig
   ```

2. **Separate read model** (query-optimized)
   ```java
   public class SurveyReadModel {
       private String id;
       private String title;
       private String status;
       // ... no commands, only data
   }
   ```

3. **Event-driven sync** (if needed)
   - When survey-command publishes event
   - survey-query subscribes and updates read model
   - No direct database sharing

## Current Implementation Status

| Feature | Status | Details |
|---------|--------|---------|
| Command Port | ✅ | CreateSurveyUseCase |
| Command Implementation | ✅ | CreateSurveyService |
| Domain Aggregate | ✅ | Survey with invariants |
| Domain Validation | ✅ | All invariants enforced |
| Repository Port | ✅ | Write-only (no queries) |
| Persistence Adapter | ✅ | InMemorySurveyRepository |
| REST Adapter | ✅ | SurveyController (commands only) |
| Query Port | ❌ | Intentionally omitted |
| Query Service | ❌ | Create separate service |
| Query Adapter | ❌ | Create separate service |
| Event Publishing | ❌ | Future enhancement |

## References

- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://en.wikipedia.org/wiki/Domain-driven_design)
- [Aggregate Pattern](https://martinfowler.com/bliki/DDD_Aggregate.html)

---

**Last Updated**: 2026-01-19  
**Version**: 1.1 - CQRS & Hexagonal Boundaries  
**Java Version**: 17  
**Spring Boot Version**: 3.x
