# Test Plan - Eventy Event Service

## Document Information
- **Project**: Eventy Event Service
- **Version**: 1.0
- **Date**: 2025-01-29
- **Author**: Development Team

---

## 1. Test Objectives

### Primary Goals
- Validate all event management functionalities (CRUD operations)
- Ensure data integrity and business rule enforcement
- Verify API endpoints behavior and error handling
- Test database constraints and relationships
- Validate input validation and security

### Success Criteria
- 100% of unit tests passing
- 100% of integration tests passing
- Code coverage > 80%
- All critical user flows validated
- No P0/P1 bugs in production

---

## 2. Test Scope

### In Scope

#### Functional Testing
- **Event Management**
  - Create event with valid/invalid data
  - Update event information
  - Delete event
  - Retrieve event by ID
  - List all events
  - Filter events by status (active, canceled, full)
  - Filter events by creator
  - Retrieve upcoming events

- **Event Types & Categories**
  - CRUD operations for event types
  - CRUD operations for event categories
  - Unique label constraints

- **Favorites Management**
  - Add event to favorites
  - Remove event from favorites
  - List user favorites
  - Prevent duplicate favorites (uniqueness constraint)
  - Cascade delete when event removed

#### Data Validation Testing
- Event name validation (required, max length)
- Date validation (required, coherence: end >= start)
- Creator ID validation (required)
- Status validation (valid enum values)
- Email/URL format validation (if applicable)

#### Database Testing
- Primary key generation (UUID)
- Foreign key constraints
- Unique constraints (event_type.label, event_category.label, favorites combination)
- Check constraints (dates, status values)
- Cascade delete behavior
- Index performance

#### API Testing
- HTTP status codes (200, 201, 204, 400, 404)
- Request/Response JSON format
- Content-Type headers
- CORS configuration
- Error response format

### Out of Scope (for this phase)
- Authentication/Authorization (handled by separate Auth service)
- Payment processing (separate service)
- Ticket management (separate service)
- Performance/Load testing (separate test phase)
- UI/UX testing (frontend responsibility)

---

## 3. Test Types & Levels

### 3.1 Unit Tests (White Box)

**Objective**: Test individual components in isolation

#### Model Layer Tests
- ✅ `EventTest.java` - Event entity validation
  - Required field validation
  - Field length constraints
  - Date coherence
  - Default values (status, creationDate)
  - Enum validation

- ✅ `FavoriteTest.java` - Favorite entity validation
  - Required fields
  - Default added date
  - Event relationship

**Coverage**: Entity validation, business logic, constraints

---

### 3.2 Integration Tests (Gray Box)

**Objective**: Test interactions between components and database

#### Repository Layer Tests
- ✅ `EventRepositoryTest.java` - Event database operations
  - Save and retrieve events
  - Find by status
  - Find by creator
  - Find upcoming events
  - Update operations
  - Delete operations
  - Relationship handling (type, category)
  - Date constraints

- ✅ `FavoriteRepositoryTest.java` - Favorite database operations
  - Save and retrieve favorites
  - Find by user ID
  - Find by user and event
  - Delete operations
  - Cascade delete behavior
  - Uniqueness constraints

**Coverage**: Database operations, queries, constraints, relationships

---

### 3.3 API Integration Tests (Black Box)

**Objective**: Test complete HTTP endpoints with database

#### Controller Tests
- ✅ `EventControllerIntegrationTest.java` - Event REST API
  - GET /events (list all)
  - GET /events/{id} (retrieve by ID)
  - GET /events/status/{status} (filter by status)
  - GET /events/creator/{creatorId} (filter by creator)
  - GET /events/upcoming (upcoming events)
  - POST /events (create event)
  - PUT /events/{id} (update event)
  - PATCH /events/{id}/status (update status)
  - DELETE /events/{id} (delete event)
  - Error scenarios (404, 400)
  - Validation errors
  - Default values

- ✅ `FavoriteControllerIntegrationTest.java` - Favorite REST API
  - GET /favorites/user/{userId} (user favorites)
  - GET /favorites/{id} (retrieve by ID)
  - POST /favorites (add favorite)
  - DELETE /favorites/{id} (remove by ID)
  - DELETE /favorites/user/{userId}/event/{eventId} (remove by combination)
  - Multiple favorites per user
  - Multiple users per event
  - Cascade delete (via ON DELETE CASCADE in database)
    - Uses non-transactional test to verify database-level cascade behavior

**Coverage**: HTTP endpoints, request/response, error handling, status codes

---

## 4. Test Data Strategy

### Test Data Categories

#### Valid Data Sets
- **Events**: Various types (concerts, sports, conferences)
- **Dates**: Past, present, future, same-day, multi-day
- **Statuses**: active, canceled, full
- **UUIDs**: Valid creator IDs, event IDs

#### Invalid Data Sets
- **Empty/Null values**: blank names, null dates, null IDs
- **Length violations**: names > 255 characters
- **Date violations**: end before start
- **Invalid enums**: non-existent status values

#### Edge Cases
- **Boundary dates**: today, far future (2050+)
- **Special characters**: unicode in names, descriptions
- **Large datasets**: pagination limits

### Data Management
- **Setup**: `@BeforeEach` methods create clean test data
- **Cleanup**: `@Transactional` rollback after each test (except cascade delete test)
- **Isolation**: Each test creates own data, no shared state

### Special Test Cases

#### Cascade Delete Testing
The cascade delete test (`shouldCascadeDeleteWhenEventDeleted`) requires special handling:
- **Challenge**: Testing database-level cascade delete behavior
- **Solution**: Uses `@Transactional(propagation = NOT_SUPPORTED)` to disable Spring's transaction management
- **Rationale**: 
  - SQL `ON DELETE CASCADE` is a database constraint
  - When all operations occur in a single Hibernate session, cascade may be handled by Hibernate instead of the database
  - Disabling transaction propagation ensures each operation commits separately
  - This verifies that the database constraint works correctly in production scenarios
- **Implementation**: Test creates event and favorite, deletes event via API, then verifies favorite is automatically removed

---

## 5. Test Execution Plan

### 5.1 Development Phase (Continuous)
```
Developer writes code
    ↓
Developer writes unit tests
    ↓
Run tests locally: mvn test
    ↓
Code review with test coverage check
    ↓
Commit to Git (triggers CI)
```

### 5.2 CI/CD Pipeline (Automated)
```
Git push
    ↓
GitHub Actions / Jenkins
    ↓
Build: mvn clean package
    ↓
Run all tests: mvn test
    ↓
Generate coverage report (JaCoCo)
    ↓
Quality gate check (SonarQube)
    ↓
If pass: Deploy to staging
```

### 5.3 Pre-Release (Manual)
```
Run full regression suite
    ↓
Manual exploratory testing
    ↓
Performance smoke tests
    ↓
Security scan (OWASP ZAP)
    ↓
Sign-off
```

---

## 6. Test Environment

### Local Development
- **JDK**: 21
- **Maven**: 3.9+
- **Database**: H2 in-memory (tests)
- **IDE**: IntelliJ IDEA / Eclipse

### CI Environment
- **Build Server**: GitHub Actions / Jenkins
- **Database**: H2 in-memory
- **JDK**: 21
- **Maven**: 3.9+

### Staging/Integration
- **Database**: PostgreSQL 15 (Docker)
- **App Server**: Docker container
- **Network**: Docker Compose network

---

## 7. Tools & Frameworks

### Testing Frameworks
- **JUnit 5**: Test execution framework
- **Spring Boot Test**: Integration testing support
- **MockMvc**: HTTP request/response testing
- **AssertJ / Hamcrest**: Assertions
- **H2 Database**: In-memory database for tests

### Coverage & Quality
- **JaCoCo**: Code coverage analysis
- **SonarQube**: Code quality metrics
- **Maven Surefire**: Test reporting

### Documentation
- **JavaDoc**: Test documentation
- **@DisplayName**: Human-readable test descriptions
- **This document**: Overall test strategy

---

## 8. Test Metrics & KPIs

### Code Coverage Targets
- **Unit Tests**: > 90% line coverage
- **Integration Tests**: > 80% line coverage
- **Overall**: > 85% combined coverage

### Quality Metrics
- **Test Success Rate**: 100% (all tests must pass)
- **Build Time**: < 5 minutes (including tests)
- **Test Execution Time**: < 2 minutes
- **Defect Density**: < 1 defect per 1000 LOC

### Tracking
- **Test Results**: Maven Surefire reports
- **Coverage**: JaCoCo HTML reports
- **Trends**: SonarQube dashboard
- **Bugs**: Jira ticket tracking

---

## 9. Test Cases Summary

### Unit Tests (Model Layer)
| Test Class | Test Cases | Focus |
|------------|-----------|--------|
| EventTest | 12 | Entity validation, constraints, defaults |
| FavoriteTest | 5 | Entity validation, relationships |
| **Total** | **17** | **Model validation** |

### Integration Tests (Repository Layer)
| Test Class | Test Cases | Focus |
|------------|-----------|--------|
| EventRepositoryTest | 10 | DB operations, queries, constraints |
| FavoriteRepositoryTest | 11 | DB operations, uniqueness, cascade |
| **Total** | **21** | **Data layer** |

### API Integration Tests (Controller Layer)
| Test Class | Test Cases | Focus |
|------------|-----------|--------|
| EventControllerIntegrationTest | 21 | REST endpoints, validation, errors |
| FavoriteControllerIntegrationTest | 11 | REST endpoints, cascade, uniqueness |
| **Total** | **32** | **API layer** |

### Grand Total
**70 automated tests** covering:
- Event CRUD operations
- Favorite management
- Data validation
- Database constraints
- API behavior
- Error handling

---

## 10. Test Execution Commands

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=EventControllerIntegrationTest
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
```bash
# Report location: target/site/jacoco/index.html
open target/site/jacoco/index.html  # macOS
start target/site/jacoco/index.html # Windows
```

### Run in Debug Mode
```bash
mvn test -Dmaven.surefire.debug
```

---

## 11. Defect Management

### Bug Severity Levels
- **P0 (Critical)**: System crash, data loss, security breach
- **P1 (High)**: Major feature broken, no workaround
- **P2 (Medium)**: Feature partially broken, workaround exists
- **P3 (Low)**: Minor issue, cosmetic

### Bug Workflow
```
Bug found in testing
    ↓
Create Jira ticket with:
  - Steps to reproduce
  - Expected vs actual result
  - Test case that failed
  - Environment info
    ↓
Assign to developer
    ↓
Developer fixes + adds regression test
    ↓
Code review
    ↓
Retest by QA
    ↓
Close ticket
```

---

## 12. Risks & Mitigation

### Identified Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Database migration failures | High | Medium | Thorough Flyway testing, rollback scripts |
| Test data conflicts | Medium | Low | Use @Transactional, isolated test data |
| Flaky tests (timing issues) | Medium | Medium | Use TestContainers, avoid Thread.sleep |
| Low test coverage | High | Low | Mandatory coverage gates in CI |
| Breaking API changes | High | Medium | Versioned APIs, contract testing |

---

## 13. Test Schedule (8-week MVP)

| Week | Activity | Deliverable |
|------|----------|-------------|
| 1-2 | Setup test infrastructure, write unit tests | 17 unit tests passing |
| 3-4 | Repository integration tests | 21 integration tests passing |
| 5-6 | Controller integration tests | 32 API tests passing |
| 7 | Performance & security testing | Test reports |
| 8 | Regression testing, bug fixes | All tests passing, documentation |

---

## 14. Continuous Improvement

### Test Review Cycle
- **Weekly**: Review failed tests, flaky tests
- **Sprint End**: Analyze coverage gaps
- **Monthly**: Update test plan based on learnings

### Test Automation Goals
- Expand to performance tests (JMeter)
- Add contract tests (Pact)
- Implement mutation testing (PIT)
- Add security tests (OWASP ZAP automation)

---

## 15. Sign-off

### Stakeholder Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Development Lead | _______ | _______ | _____ |
| QA Lead | _______ | _______ | _____ |
| Product Owner | _______ | _______ | _____ |

---

## Appendix A: Test Naming Conventions

### Pattern
```java
@Test
@DisplayName("Should [expected behavior] when [condition]")
void should[ExpectedBehavior]When[Condition]() {
    // Given (setup)
    // When (action)
    // Then (assertion)
}
```

### Examples
- `shouldReturnAllEventsWhenCalled()`
- `shouldReturn404WhenEventNotFound()`
- `shouldFailWhenEventNameIsBlank()`

---

## Appendix B: Test Data Builders

### Event Builder
```java
private Event createValidEvent() {
    Event event = new Event();
    event.setName("Test Event");
    event.setStartDate(LocalDate.now().plusDays(10));
    event.setEndDate(LocalDate.now().plusDays(12));
    event.setCreatorId(UUID.randomUUID());
    // ... other fields
    return event;
}
```

---

*End of Test Plan*
# Test configuration for H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA configuration for tests
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Disable Flyway for tests (use Hibernate DDL)
spring.flyway.enabled=false

# H2 console (optional, for debugging)
spring.h2.console.enabled=true

