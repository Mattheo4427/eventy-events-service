package com.eventy.eventyeventservice.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Event entity
 * Tests validation rules, business logic, and constraints
 */
@DisplayName("Event Entity Unit Tests")
class EventTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid event with all required fields")
    void shouldCreateValidEvent() {
        // Given
        Event event = createValidEvent();

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertTrue(violations.isEmpty(), "Valid event should have no violations");
    }

    @Test
    @DisplayName("Should fail when event name is blank")
    void shouldFailWhenNameIsBlank() {
        // Given
        Event event = createValidEvent();
        event.setName("");

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Event name is required")));
    }

    @Test
    @DisplayName("Should fail when event name exceeds 255 characters")
    void shouldFailWhenNameTooLong() {
        // Given
        Event event = createValidEvent();
        event.setName("A".repeat(256));

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("cannot exceed 255 characters")));
    }

    @Test
    @DisplayName("Should fail when start date is null")
    void shouldFailWhenStartDateIsNull() {
        // Given
        Event event = createValidEvent();
        event.setStartDate(null);

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Start date is required")));
    }

    @Test
    @DisplayName("Should fail when end date is null")
    void shouldFailWhenEndDateIsNull() {
        // Given
        Event event = createValidEvent();
        event.setEndDate(null);

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("End date is required")));
    }

    @Test
    @DisplayName("Should fail when creator ID is null")
    void shouldFailWhenCreatorIdIsNull() {
        // Given
        Event event = createValidEvent();
        event.setCreatorId(null);

        // When
        Set<ConstraintViolation<Event>> violations = validator.validate(event);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Creator ID is required")));
    }

    @Test
    @DisplayName("Should set default status to active")
    void shouldSetDefaultStatusToActive() {
        // Given & When
        Event event = new Event();

        // Then
        assertEquals(EventStatus.active, event.getStatus());
    }

    @Test
    @DisplayName("Should set default creation date to today")
    void shouldSetDefaultCreationDateToToday() {
        // Given & When
        Event event = new Event();

        // Then
        assertEquals(LocalDate.now(), event.getCreationDate());
    }

    @Test
    @DisplayName("Should accept all valid event statuses")
    void shouldAcceptAllValidStatuses() {
        // Given
        Event event = createValidEvent();

        // When & Then - Test each status
        event.setStatus(EventStatus.active);
        assertTrue(validator.validate(event).isEmpty());

        event.setStatus(EventStatus.canceled);
        assertTrue(validator.validate(event).isEmpty());

        event.setStatus(EventStatus.full);
        assertTrue(validator.validate(event).isEmpty());
    }

    @Test
    @DisplayName("Should create event with event type and category")
    void shouldCreateEventWithTypeAndCategory() {
        // Given
        Event event = createValidEvent();
        EventType eventType = new EventType();
        eventType.setEventTypeId(UUID.randomUUID());
        eventType.setLabel("Concert");

        EventCategory category = new EventCategory();
        category.setCategoryId(UUID.randomUUID());
        category.setLabel("Music");

        // When
        event.setEventType(eventType);
        event.setCategory(category);

        // Then
        assertNotNull(event.getEventType());
        assertNotNull(event.getCategory());
        assertEquals("Concert", event.getEventType().getLabel());
        assertEquals("Music", event.getCategory().getLabel());
    }

    @Test
    @DisplayName("Should handle dates coherently - end date after start date")
    void shouldHandleDatesCoherently() {
        // Given
        Event event = createValidEvent();
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 10);

        // When
        event.setStartDate(startDate);
        event.setEndDate(endDate);

        // Then
        assertTrue(event.getEndDate().isAfter(event.getStartDate()) ||
                   event.getEndDate().isEqual(event.getStartDate()));
    }

    @Test
    @DisplayName("Should handle same day event - start and end date equal")
    void shouldHandleSameDayEvent() {
        // Given
        Event event = createValidEvent();
        LocalDate eventDate = LocalDate.of(2025, 12, 1);

        // When
        event.setStartDate(eventDate);
        event.setEndDate(eventDate);

        // Then
        assertEquals(event.getStartDate(), event.getEndDate());
        assertTrue(validator.validate(event).isEmpty());
    }

    /**
     * Helper method to create a valid event for testing
     */
    private Event createValidEvent() {
        Event event = new Event();
        event.setName("Jazz Festival");
        event.setDescription("Annual jazz music festival");
        event.setStartDate(LocalDate.now().plusDays(30));
        event.setEndDate(LocalDate.now().plusDays(32));
        event.setLocation("Paris");
        event.setFullAddress("Parc des Buttes-Chaumont, 75019 Paris");
        event.setImageUrl("https://example.com/event.jpg");
        event.setStatus(EventStatus.active);
        event.setCreatorId(UUID.randomUUID());
        event.setCreationDate(LocalDate.now());
        return event;
    }
}

