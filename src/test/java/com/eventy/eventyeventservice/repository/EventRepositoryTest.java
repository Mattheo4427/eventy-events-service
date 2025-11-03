package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
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
 * Integration tests for EventRepository
 * Tests database operations and custom queries
 */
@DataJpaTest
@DisplayName("Event Repository Integration Tests")
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    private EventType concertType;
    private EventCategory musicCategory;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        // Setup test data
        concertType = new EventType();
        concertType.setLabel("Concert");
        concertType = eventTypeRepository.save(concertType);

        musicCategory = new EventCategory();
        musicCategory.setLabel("Music");
        musicCategory = eventCategoryRepository.save(musicCategory);

        creatorId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and retrieve event")
    void shouldSaveAndRetrieveEvent() {
        // Given
        Event event = createTestEvent("Jazz Night", EventStatus.active);

        // When
        Event savedEvent = eventRepository.save(event);
        Optional<Event> retrieved = eventRepository.findById(savedEvent.getEventId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals("Jazz Night", retrieved.get().getName());
        assertEquals(EventStatus.active, retrieved.get().getStatus());
    }

    @Test
    @DisplayName("Should find events by status")
    void shouldFindEventsByStatus() {
        // Given
        createTestEvent("Active Event 1", EventStatus.active);
        createTestEvent("Active Event 2", EventStatus.active);
        createTestEvent("Canceled Event", EventStatus.canceled);
        entityManager.flush();

        // When
        List<Event> activeEvents = eventRepository.findByStatus(EventStatus.active);
        List<Event> canceledEvents = eventRepository.findByStatus(EventStatus.canceled);

        // Then
        assertEquals(2, activeEvents.size());
        assertEquals(1, canceledEvents.size());
        assertTrue(activeEvents.stream().allMatch(e -> e.getStatus() == EventStatus.active));
    }

    @Test
    @DisplayName("Should find events by creator ID")
    void shouldFindEventsByCreatorId() {
        // Given
        UUID creator1 = UUID.randomUUID();
        UUID creator2 = UUID.randomUUID();

        Event event1 = createTestEvent("Event 1", EventStatus.active);
        event1.setCreatorId(creator1);
        eventRepository.save(event1);

        Event event2 = createTestEvent("Event 2", EventStatus.active);
        event2.setCreatorId(creator1);
        eventRepository.save(event2);

        Event event3 = createTestEvent("Event 3", EventStatus.active);
        event3.setCreatorId(creator2);
        eventRepository.save(event3);

        entityManager.flush();

        // When
        List<Event> creator1Events = eventRepository.findByCreatorId(creator1);
        List<Event> creator2Events = eventRepository.findByCreatorId(creator2);

        // Then
        assertEquals(2, creator1Events.size());
        assertEquals(1, creator2Events.size());
    }

    @Test
    @DisplayName("Should find upcoming events after specific date")
    void shouldFindUpcomingEvents() {
        // Given
        LocalDate today = LocalDate.now();

        Event pastEvent = createTestEvent("Past Event", EventStatus.active);
        pastEvent.setStartDate(today.minusDays(10));
        pastEvent.setEndDate(today.minusDays(8));
        eventRepository.save(pastEvent);

        Event futureEvent1 = createTestEvent("Future Event 1", EventStatus.active);
        futureEvent1.setStartDate(today.plusDays(5));
        futureEvent1.setEndDate(today.plusDays(7));
        eventRepository.save(futureEvent1);

        Event futureEvent2 = createTestEvent("Future Event 2", EventStatus.active);
        futureEvent2.setStartDate(today.plusDays(15));
        futureEvent2.setEndDate(today.plusDays(17));
        eventRepository.save(futureEvent2);

        entityManager.flush();

        // When
        List<Event> upcomingEvents = eventRepository.findByStartDateAfter(today);

        // Then
        assertEquals(2, upcomingEvents.size());
        assertTrue(upcomingEvents.stream()
            .allMatch(e -> e.getStartDate().isAfter(today)));
    }

    @Test
    @DisplayName("Should update event status")
    void shouldUpdateEventStatus() {
        // Given
        Event event = createTestEvent("Test Event", EventStatus.active);
        Event savedEvent = eventRepository.save(event);

        // When
        savedEvent.setStatus(EventStatus.full);
        eventRepository.save(savedEvent);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Event> updated = eventRepository.findById(savedEvent.getEventId());
        assertTrue(updated.isPresent());
        assertEquals(EventStatus.full, updated.get().getStatus());
    }

    @Test
    @DisplayName("Should delete event")
    void shouldDeleteEvent() {
        // Given
        Event event = createTestEvent("Event to Delete", EventStatus.active);
        Event savedEvent = eventRepository.save(event);
        UUID eventId = savedEvent.getEventId();

        // When
        eventRepository.deleteById(eventId);
        entityManager.flush();

        // Then
        Optional<Event> deleted = eventRepository.findById(eventId);
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Should handle event with type and category relationships")
    void shouldHandleEventWithRelationships() {
        // Given
        Event event = createTestEvent("Concert Event", EventStatus.active);
        event.setEventType(concertType);
        event.setCategory(musicCategory);

        // When
        Event savedEvent = eventRepository.save(event);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Event> retrieved = eventRepository.findById(savedEvent.getEventId());
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getEventType());
        assertNotNull(retrieved.get().getCategory());
        assertEquals("Concert", retrieved.get().getEventType().getLabel());
        assertEquals("Music", retrieved.get().getCategory().getLabel());
    }

    @Test
    @DisplayName("Should enforce event date constraints")
    void shouldEnforceEventDateConstraints() {
        // Given
        Event event = createTestEvent("Test Event", EventStatus.active);
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 10);

        event.setStartDate(startDate);
        event.setEndDate(endDate);

        // When
        Event savedEvent = eventRepository.save(event);

        // Then
        assertTrue(savedEvent.getEndDate().isAfter(savedEvent.getStartDate()) ||
                   savedEvent.getEndDate().isEqual(savedEvent.getStartDate()));
    }

    @Test
    @DisplayName("Should count events by status")
    void shouldCountEventsByStatus() {
        // Given
        createTestEvent("Active 1", EventStatus.active);
        createTestEvent("Active 2", EventStatus.active);
        createTestEvent("Full 1", EventStatus.full);
        entityManager.flush();

        // When
        long activeCount = eventRepository.findByStatus(EventStatus.active).size();
        long fullCount = eventRepository.findByStatus(EventStatus.full).size();

        // Then
        assertEquals(2, activeCount);
        assertEquals(1, fullCount);
    }

    private Event createTestEvent(String name, EventStatus status) {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Test event description");
        event.setStartDate(LocalDate.now().plusDays(30));
        event.setEndDate(LocalDate.now().plusDays(32));
        event.setLocation("Paris");
        event.setFullAddress("Test Address, Paris");
        event.setStatus(status);
        event.setCreatorId(creatorId);
        event.setCreationDate(LocalDate.now());
        entityManager.persist(event);
        return event;
    }
}

