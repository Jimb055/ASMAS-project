# Survey Command Service - Hexagonal Architecture Refactoring

## Overview
This service has been refactored to follow **Hexagonal Architecture** (Ports & Adapters) with **CQRS (Command side only)**.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    HTTP Request/Response                 │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   IN ADAPTER (REST)         │
        │ adapter.in.rest.            │
        │ SurveyController            │
        └──────────────┬──────────────┘
                       │ (translates HTTP → Commands)
        ┌──────────────▼──────────────┐
        │  PORT (Inbound)             │
        │ domain.port.in.             │
        │ CreateSurveyUseCase         │
        └──────────────┬──────────────┘
                       │ (business contract)
        ┌──────────────▼──────────────┐
        │ APPLICATION SERVICE         │
        │ application.service.        │
        │ CreateSurveyService         │
        └──────────────┬──────────────┘
                       │ (implements use case)
        ┌──────────────▼──────────────┐
        │  DOMAIN MODEL               │
        │ domain.model.*              │
        │ (No Spring annotations)     │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │  PORT (Outbound)            │
        │ domain.port.out.            │
        │ SurveyRepository            │
        └──────────────┬──────────────┘
                       │ (persistence contract)
        ┌──────────────▼──────────────┐
        │ OUT ADAPTER (Persistence)   │
        │ adapter.out.persistence.    │
        │ InMemorySurveyRepository    │
        └─────────────────────────────┘
```

## Package Structure

```
com.asmas.surveycommand
├─ domain/
│   ├─ model/
│   │   ├─ Survey                    (Entity - pure business logic)
│   │   ├─ SurveyStatus              (Value Object - enum)
│   │   └─ Question                  (Value Object)
│   └─ port/
│       ├─ in/
│       │   └─ CreateSurveyUseCase   (Inbound port interface)
│       └─ out/
│           └─ SurveyRepository      (Outbound port interface)
│
├─ application/
│   └─ service/
│       └─ CreateSurveyService       (Implements CreateSurveyUseCase)
│
├─ adapter/
│   ├─ in/
│   │   └─ rest/
│   │       └─ SurveyController      (REST inbound adapter)
│   └─ out/
│       └─ persistence/
│           └─ InMemorySurveyRepository (Persistence outbound adapter)
│
└─ config/
    └─ HexagonalArchitectureConfig   (Spring dependency injection)
```

## Key Design Principles

### 1. Domain Layer (No Spring Annotations)
- **Survey**: Aggregate root with business logic
- **SurveyStatus**: Value object representing state
- **Question**: Value object
- All classes are pure POJOs with NO Spring dependencies

### 2. Domain Ports
- **Inbound (CreateSurveyUseCase)**: Defines what commands the system accepts
- **Outbound (SurveyRepository)**: Defines what persistence operations are needed

### 3. Application Layer
- **CreateSurveyService**: Implements the use case, orchestrates domain logic
- Acts as the bridge between adapters and domain

### 4. Adapter Layer (Inbound - REST)
- **SurveyController**: Translates HTTP requests to domain commands
- Only concern: HTTP protocol handling
- Delegates business logic to application services

### 5. Adapter Layer (Outbound - Persistence)
- **InMemorySurveyRepository**: Implements the repository port
- Currently in-memory; can be replaced with JPA/Database without affecting domain or application layers

### 6. Configuration
- **HexagonalArchitectureConfig**: Spring configuration for dependency injection
- Wires together domain, application, and adapters

## API Endpoints

### Health Check (Existing - Still Working)
```
GET /protected/survey/ping
Headers:
  X-Username: string
  X-User-Id: string
  
Response: 200 OK
  "Authenticated request from user=..., id=..."
```

### Create Survey (New - CQRS Command)
```
POST /protected/survey
Headers:
  X-User-Id: string (becomes createdBy)
  
Request Body:
{
  "title": "Customer Satisfaction Survey",
  "description": "Quarterly feedback collection"
}

Response: 201 Created
{
  "surveyId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Customer Satisfaction Survey",
  "status": "DRAFT"
}
```

## Migration Benefits

### Testability
- Domain models can be unit tested without Spring context
- Application services are easily mockable
- Adapters can be tested independently

### Maintainability
- Clear separation of concerns
- Business logic is isolated in domain
- Easy to locate code by responsibility

### Flexibility
- Persistence can be swapped: In-Memory → JPA → MongoDB
- Additional adapters can be added: gRPC, messaging, etc.
- No changes needed to domain or application layers

### Scalability (Future)
- Ready for database integration
- Ready for event sourcing (append inbound port)
- Ready for async command processing

## Future Extensions

1. **Add Query Side (CQRS Read)**
   - Create `FindSurveyByIdQuery` use case
   - Separate read model from write model

2. **Persist to Database**
   - Replace `InMemorySurveyRepository` with `JpaSurveyRepository`
   - No changes needed to domain or application layers

3. **Add More Commands**
   - `PublishSurveyCommand`
   - `ArchiveSurveyCommand`
   - `AddQuestionCommand`

4. **Add Events** (if needed later)
   - `SurveyCreatedEvent`
   - `SurveyPublishedEvent`
   - Outbound port: `EventPublisher`

## Running the Service

```bash
# Build
mvn clean package

# Run
java -jar target/survey-command-0.0.1-SNAPSHOT.jar

# Health check
curl -H "X-Username: test" -H "X-User-Id: user123" \
     http://localhost:8082/protected/survey/ping

# Create survey
curl -X POST http://localhost:8082/protected/survey \
     -H "X-User-Id: user123" \
     -H "Content-Type: application/json" \
     -d '{"title": "Test Survey", "description": "Test Description"}'
```

## Testing Strategy

### Domain Testing (No Spring needed)
```java
public class SurveyTest {
    @Test
    void testSurveyCreation() {
        Survey survey = Survey.create("Title", "Desc", "user1");
        assertEquals("user1", survey.getCreatedBy());
        assertEquals(SurveyStatus.DRAFT, survey.getStatus());
    }
}
```

### Application Service Testing (Mock repository)
```java
public class CreateSurveyServiceTest {
    @Test
    void testCreateSurvey() {
        SurveyRepository mockRepo = mock(SurveyRepository.class);
        CreateSurveyService service = new CreateSurveyService(mockRepo);
        
        CreateSurveyResponse response = service.execute(
            new CreateSurveyCommand("Title", "Desc", "user1")
        );
        
        verify(mockRepo).save(any(Survey.class));
    }
}
```

### Controller Testing (Spring Test)
```java
@SpringBootTest
public class SurveyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateSurvey() throws Exception {
        mockMvc.perform(post("/protected/survey")
            .header("X-User-Id", "user123")
            .contentType(APPLICATION_JSON)
            .content("{\"title\": \"Test\", \"description\": \"Desc\"}"))
            .andExpect(status().isCreated());
    }
}
```

---

**Last Updated**: 2026-01-19  
**Architecture**: Hexagonal Architecture + CQRS (Command side)  
**Java Version**: 17  
**Spring Boot Version**: 3.x
