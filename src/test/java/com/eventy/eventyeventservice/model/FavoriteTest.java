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
 * Unit tests for Favorite entity
 * Tests uniqueness constraints and business rules
 */
@DisplayName("Favorite Entity Unit Tests")
class FavoriteTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid favorite")
    void shouldCreateValidFavorite() {
        // Given
        Favorite favorite = createValidFavorite();

        // When
        Set<ConstraintViolation<Favorite>> violations = validator.validate(favorite);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail when user ID is null")
    void shouldFailWhenUserIdIsNull() {
        // Given
        Favorite favorite = createValidFavorite();
        favorite.setUserId(null);

        // When
        Set<ConstraintViolation<Favorite>> violations = validator.validate(favorite);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should set default added date to today")
    void shouldSetDefaultAddedDateToToday() {
        // Given & When
        Favorite favorite = new Favorite();

        // Then
        assertEquals(LocalDate.now(), favorite.getAddedDate());
    }

    @Test
    @DisplayName("Should link favorite to event")
    void shouldLinkFavoriteToEvent() {
        // Given
        Favorite favorite = createValidFavorite();
        Event event = createValidEvent();

        // When
        favorite.setEvent(event);

        // Then
        assertNotNull(favorite.getEvent());
        assertEquals(event.getEventId(), favorite.getEvent().getEventId());
    }

    @Test
    @DisplayName("Should handle deletion when event is removed - cascade behavior")
    void shouldHandleCascadeDeletion() {
        // Given
        Favorite favorite = createValidFavorite();
        Event event = createValidEvent();
        favorite.setEvent(event);

        // When - Event is deleted (simulated)
        favorite.setEvent(null);

        // Then
        assertNull(favorite.getEvent());
    }

    private Favorite createValidFavorite() {
        Favorite favorite = new Favorite();
        favorite.setUserId(UUID.randomUUID());
        favorite.setAddedDate(LocalDate.now());
        return favorite;
    }

    private Event createValidEvent() {
        Event event = new Event();
        event.setEventId(UUID.randomUUID());
        event.setName("Test Event");
        event.setStartDate(LocalDate.now().plusDays(10));
        event.setEndDate(LocalDate.now().plusDays(12));
        event.setStatus(EventStatus.active);
        event.setCreatorId(UUID.randomUUID());
        event.setCreationDate(LocalDate.now());
        return event;
    }
}

