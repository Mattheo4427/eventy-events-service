package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.Favorite;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.FavoriteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FavoriteController
 * Tests favorite management and uniqueness constraints
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Favorite Controller Integration Tests")
class FavoriteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Event testEvent;
    private UUID userId;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        eventRepository.deleteAll();

        userId = UUID.randomUUID();

        // Create test event
        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartDate(LocalDate.now().plusDays(10));
        testEvent.setEndDate(LocalDate.now().plusDays(12));
        testEvent.setLocation("Paris");
        testEvent.setStatus(EventStatus.active);
        testEvent.setCreatorId(UUID.randomUUID());
        testEvent.setCreationDate(LocalDate.now());
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @DisplayName("GET /favorites/user/{userId} - Should return empty list when user has no favorites")
    void shouldReturnEmptyListWhenUserHasNoFavorites() throws Exception {
        // Given
        UUID newUserId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/favorites/user/{userId}", newUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /favorites/user/{userId} - Should return all user favorites")
    void shouldReturnAllUserFavorites() throws Exception {
        // Given
        Event event1 = createEvent("Event 1");
        Event event2 = createEvent("Event 2");
        createFavorite(userId, event1);
        createFavorite(userId, event2);

        // When & Then
        mockMvc.perform(get("/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("POST /favorites - Should add event to favorites")
    void shouldAddEventToFavorites() throws Exception {
        // Given
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEvent(testEvent);

        // When & Then
        mockMvc.perform(post("/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favorite)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.favoriteId", notNullValue()))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.addedDate", notNullValue()));
    }

    @Test
    @DisplayName("POST /favorites - Should set default added date if not provided")
    void shouldSetDefaultAddedDate() throws Exception {
        // Given
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEvent(testEvent);
        // Not setting addedDate

        // When & Then
        mockMvc.perform(post("/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favorite)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addedDate", is(LocalDate.now().toString())));
    }

    @Test
    @DisplayName("POST /favorites - Should fail when user ID is null")
    void shouldFailWhenUserIdIsNull() throws Exception {
        // Given
        Favorite favorite = new Favorite();
        favorite.setEvent(testEvent);
        // Missing userId

        // When & Then
        mockMvc.perform(post("/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favorite)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /favorites/{id} - Should remove favorite by ID")
    void shouldRemoveFavoriteById() throws Exception {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);

        // When & Then
        mockMvc.perform(delete("/favorites/{id}", favorite.getFavoriteId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/favorites/{id}", favorite.getFavoriteId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /favorites/{id} - Should return 404 when favorite not found")
    void shouldReturn404WhenFavoriteNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/favorites/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /favorites/user/{userId}/event/{eventId} - Should remove favorite by user and event")
    void shouldRemoveFavoriteByUserAndEvent() throws Exception {
        // Given
        createFavorite(userId, testEvent);

        // When & Then
        mockMvc.perform(delete("/favorites/user/{userId}/event/{eventId}",
                        userId, testEvent.getEventId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /favorites/{id} - Should return favorite by ID")
    void shouldReturnFavoriteById() throws Exception {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);

        // When & Then
        mockMvc.perform(get("/favorites/{id}", favorite.getFavoriteId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoriteId", is(favorite.getFavoriteId().toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())));
    }

    @Test
    @DisplayName("POST /favorites - Should allow same user to favorite multiple events")
    void shouldAllowSameUserToFavoriteMultipleEvents() throws Exception {
        // Given
        Event event1 = createEvent("Event 1");
        Event event2 = createEvent("Event 2");
        Event event3 = createEvent("Event 3");

        // When
        createFavorite(userId, event1);
        createFavorite(userId, event2);
        createFavorite(userId, event3);

        // Then
        mockMvc.perform(get("/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("POST /favorites - Should allow multiple users to favorite same event")
    void shouldAllowMultipleUsersToFavoriteSameEvent() throws Exception {
        // Given
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();

        // When
        createFavorite(user1, testEvent);
        createFavorite(user2, testEvent);
        createFavorite(user3, testEvent);

        // Then
        mockMvc.perform(get("/favorites/user/{userId}", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("DELETE /favorites - Should cascade delete when event is deleted")
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void shouldCascadeDeleteWhenEventDeleted() throws Exception {
        // Given - Create event and favorite in separate transaction
        Event event = new Event();
        event.setName("Test Event for Cascade");
        event.setDescription("Test Description");
        event.setStartDate(LocalDate.now().plusDays(10));
        event.setEndDate(LocalDate.now().plusDays(12));
        event.setLocation("Paris");
        event.setStatus(EventStatus.active);
        event.setCreatorId(UUID.randomUUID());
        event.setCreationDate(LocalDate.now());
        event = eventRepository.save(event);

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEvent(event);
        favorite.setAddedDate(LocalDate.now());
        favorite = favoriteRepository.save(favorite);

        UUID favoriteId = favorite.getFavoriteId();
        UUID eventId = event.getEventId();

        // When - Delete the event using the controller
        mockMvc.perform(delete("/events/{id}", eventId))
                .andExpect(status().isNoContent());

        // Then - Favorite should be automatically deleted due to ON DELETE CASCADE
        mockMvc.perform(get("/favorites/{id}", favoriteId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /favorites/user/{userId} - Should only return favorites for specific user")
    void shouldOnlyReturnFavoritesForSpecificUser() throws Exception {
        // Given
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        Event event1 = createEvent("Event 1");
        Event event2 = createEvent("Event 2");

        createFavorite(user1, event1);
        createFavorite(user1, event2);
        createFavorite(user2, event1);

        // When & Then
        mockMvc.perform(get("/favorites/user/{userId}", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/favorites/user/{userId}", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private Favorite createFavorite(UUID userId, Event event) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEvent(event);
        favorite.setAddedDate(LocalDate.now());
        return favoriteRepository.save(favorite);
    }

    private Event createEvent(String name) {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Description for " + name);
        event.setStartDate(LocalDate.now().plusDays(20));
        event.setEndDate(LocalDate.now().plusDays(22));
        event.setLocation("Paris");
        event.setStatus(EventStatus.active);
        event.setCreatorId(UUID.randomUUID());
        event.setCreationDate(LocalDate.now());
        return eventRepository.save(event);
    }
}

