package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.Favorite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FavoriteRepository
 * Tests favorite operations and uniqueness constraints
 */
@DataJpaTest
@DisplayName("Favorite Repository Integration Tests")
class FavoriteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private EventRepository eventRepository;

    private Event testEvent;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Create a test event
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
    @DisplayName("Should save and retrieve favorite")
    void shouldSaveAndRetrieveFavorite() {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);

        // When
        Favorite savedFavorite = favoriteRepository.save(favorite);
        Optional<Favorite> retrieved = favoriteRepository.findById(savedFavorite.getFavoriteId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(userId, retrieved.get().getUserId());
        assertEquals(testEvent.getEventId(), retrieved.get().getEvent().getEventId());
    }

    @Test
    @DisplayName("Should find all favorites by user ID")
    void shouldFindAllFavoritesByUserId() {
        // Given
        Event event1 = createEvent("Event 1");
        Event event2 = createEvent("Event 2");

        createFavorite(userId, event1);
        createFavorite(userId, event2);

        UUID otherUserId = UUID.randomUUID();
        createFavorite(otherUserId, event1);

        entityManager.flush();

        // When
        List<Favorite> userFavorites = favoriteRepository.findByUserId(userId);
        List<Favorite> otherUserFavorites = favoriteRepository.findByUserId(otherUserId);

        // Then
        assertEquals(2, userFavorites.size());
        assertEquals(1, otherUserFavorites.size());
        assertTrue(userFavorites.stream().allMatch(f -> f.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("Should find favorite by user ID and event ID")
    void shouldFindFavoriteByUserAndEvent() {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);
        entityManager.flush();

        // When
        Optional<Favorite> found = favoriteRepository.findByUserIdAndEvent_EventId(
            userId,
            testEvent.getEventId()
        );

        // Then
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
        assertEquals(testEvent.getEventId(), found.get().getEvent().getEventId());
    }

    @Test
    @DisplayName("Should not find favorite for non-existent combination")
    void shouldNotFindFavoriteForNonExistentCombination() {
        // Given
        createFavorite(userId, testEvent);
        UUID otherUserId = UUID.randomUUID();
        entityManager.flush();

        // When
        Optional<Favorite> found = favoriteRepository.findByUserIdAndEvent_EventId(
            otherUserId,
            testEvent.getEventId()
        );

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should delete favorite")
    void shouldDeleteFavorite() {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);
        UUID favoriteId = favorite.getFavoriteId();
        entityManager.flush();

        // When
        favoriteRepository.deleteById(favoriteId);
        entityManager.flush();

        // Then
        Optional<Favorite> deleted = favoriteRepository.findById(favoriteId);
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Should cascade delete favorites when event is deleted")
    void shouldCascadeDeleteFavoritesWhenEventDeleted() {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);
        UUID favoriteId = favorite.getFavoriteId();
        UUID eventId = testEvent.getEventId();
        entityManager.flush();
        entityManager.clear();

        // When
        Event eventToDelete = entityManager.find(Event.class, eventId);
        entityManager.remove(eventToDelete);
        entityManager.flush();

        // Then
        Optional<Favorite> deletedFavorite = favoriteRepository.findById(favoriteId);
        assertFalse(deletedFavorite.isPresent());
    }

    @Test
    @DisplayName("Should add multiple favorites for same user")
    void shouldAddMultipleFavoritesForSameUser() {
        // Given
        Event event1 = createEvent("Event 1");
        Event event2 = createEvent("Event 2");
        Event event3 = createEvent("Event 3");

        // When
        createFavorite(userId, event1);
        createFavorite(userId, event2);
        createFavorite(userId, event3);
        entityManager.flush();

        // Then
        List<Favorite> userFavorites = favoriteRepository.findByUserId(userId);
        assertEquals(3, userFavorites.size());
    }

    @Test
    @DisplayName("Should handle empty favorite list for user")
    void shouldHandleEmptyFavoriteListForUser() {
        // Given
        UUID newUserId = UUID.randomUUID();

        // When
        List<Favorite> favorites = favoriteRepository.findByUserId(newUserId);

        // Then
        assertTrue(favorites.isEmpty());
    }

    @Test
    @DisplayName("Should set default added date to today")
    void shouldSetDefaultAddedDateToToday() {
        // Given & When
        Favorite favorite = createFavorite(userId, testEvent);

        // Then
        assertEquals(LocalDate.now(), favorite.getAddedDate());
    }

    @Test
    @DisplayName("Should preserve added date when retrieving favorite")
    void shouldPreserveAddedDateWhenRetrieving() {
        // Given
        Favorite favorite = createFavorite(userId, testEvent);
        LocalDate addedDate = LocalDate.now().minusDays(5);
        favorite.setAddedDate(addedDate);
        favoriteRepository.save(favorite);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Favorite> retrieved = favoriteRepository.findById(favorite.getFavoriteId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(addedDate, retrieved.get().getAddedDate());
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

