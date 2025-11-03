package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
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
 * Integration tests for EventController
 * Tests complete API endpoints with database interaction
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Event Controller Integration Tests")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private EventType concertType;
    private EventCategory musicCategory;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        eventTypeRepository.deleteAll();
        eventCategoryRepository.deleteAll();

        concertType = new EventType();
        concertType.setLabel("Concert");
        concertType = eventTypeRepository.save(concertType);

        musicCategory = new EventCategory();
        musicCategory.setLabel("Music");
        musicCategory = eventCategoryRepository.save(musicCategory);

        creatorId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /events - Should return empty list when no events exist")
    void shouldReturnEmptyListWhenNoEvents() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /events - Should return all events")
    void shouldReturnAllEvents() throws Exception {
        // Given
        createTestEvent("Event 1", EventStatus.active);
        createTestEvent("Event 2", EventStatus.active);
        createTestEvent("Event 3", EventStatus.canceled);

        // When & Then
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[1].name", notNullValue()))
                .andExpect(jsonPath("$[2].name", notNullValue()));
    }

    @Test
    @DisplayName("GET /events/{id} - Should return event by ID")
    void shouldReturnEventById() throws Exception {
        // Given
        Event event = createTestEvent("Jazz Festival", EventStatus.active);

        // When & Then
        mockMvc.perform(get("/events/{id}", event.getEventId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jazz Festival")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.eventId", notNullValue()));
    }

    @Test
    @DisplayName("GET /events/{id} - Should return 404 when event not found")
    void shouldReturn404WhenEventNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/events/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /events - Should create new event with valid data")
    void shouldCreateNewEventWithValidData() throws Exception {
        // Given
        Event newEvent = new Event();
        newEvent.setName("New Concert");
        newEvent.setDescription("Amazing concert");
        newEvent.setStartDate(LocalDate.now().plusDays(30));
        newEvent.setEndDate(LocalDate.now().plusDays(32));
        newEvent.setLocation("Paris");
        newEvent.setFullAddress("123 Music Street, Paris");
        newEvent.setStatus(EventStatus.active);
        newEvent.setCreatorId(creatorId);

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId", notNullValue()))
                .andExpect(jsonPath("$.name", is("New Concert")))
                .andExpect(jsonPath("$.status", is("active")))
                .andExpect(jsonPath("$.creationDate", notNullValue()));
    }

    @Test
    @DisplayName("POST /events - Should fail when event name is blank")
    void shouldFailWhenEventNameIsBlank() throws Exception {
        // Given
        Event invalidEvent = new Event();
        invalidEvent.setName("");
        invalidEvent.setStartDate(LocalDate.now().plusDays(10));
        invalidEvent.setEndDate(LocalDate.now().plusDays(12));
        invalidEvent.setCreatorId(creatorId);

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /events - Should fail when required fields are missing")
    void shouldFailWhenRequiredFieldsMissing() throws Exception {
        // Given
        Event invalidEvent = new Event();
        invalidEvent.setName("Incomplete Event");
        // Missing startDate, endDate, creatorId

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /events/{id} - Should update existing event")
    void shouldUpdateExistingEvent() throws Exception {
        // Given
        Event existingEvent = createTestEvent("Original Event", EventStatus.active);
        existingEvent.setName("Updated Event");
        existingEvent.setLocation("Lyon");

        // When & Then
        mockMvc.perform(put("/events/{id}", existingEvent.getEventId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Event")))
                .andExpect(jsonPath("$.location", is("Lyon")));
    }

    @Test
    @DisplayName("PUT /events/{id} - Should return 404 when updating non-existent event")
    void shouldReturn404WhenUpdatingNonExistentEvent() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Event event = new Event();
        event.setName("Test Event");
        event.setStartDate(LocalDate.now().plusDays(10));
        event.setEndDate(LocalDate.now().plusDays(12));
        event.setCreatorId(creatorId);

        // When & Then
        mockMvc.perform(put("/events/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /events/{id}/status - Should update event status")
    void shouldUpdateEventStatus() throws Exception {
        // Given
        Event event = createTestEvent("Test Event", EventStatus.active);

        // When & Then
        mockMvc.perform(patch("/events/{id}/status", event.getEventId())
                        .param("status", "canceled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("canceled")));
    }

    @Test
    @DisplayName("DELETE /events/{id} - Should delete event")
    void shouldDeleteEvent() throws Exception {
        // Given
        Event event = createTestEvent("Event to Delete", EventStatus.active);

        // When & Then
        mockMvc.perform(delete("/events/{id}", event.getEventId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/events/{id}", event.getEventId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /events/{id} - Should return 404 when deleting non-existent event")
    void shouldReturn404WhenDeletingNonExistentEvent() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/events/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /events/status/{status} - Should filter events by status")
    void shouldFilterEventsByStatus() throws Exception {
        // Given
        createTestEvent("Active Event 1", EventStatus.active);
        createTestEvent("Active Event 2", EventStatus.active);
        createTestEvent("Canceled Event", EventStatus.canceled);
        createTestEvent("Full Event", EventStatus.full);

        // When & Then - Test active status
        mockMvc.perform(get("/events/status/{status}", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("active"))));

        // Test canceled status
        mockMvc.perform(get("/events/status/{status}", "canceled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("canceled")));
    }

    @Test
    @DisplayName("GET /events/creator/{creatorId} - Should filter events by creator")
    void shouldFilterEventsByCreator() throws Exception {
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

        // When & Then
        mockMvc.perform(get("/events/creator/{creatorId}", creator1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/events/creator/{creatorId}", creator2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /events/upcoming - Should return only upcoming events")
    void shouldReturnOnlyUpcomingEvents() throws Exception {
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

        // When & Then
        mockMvc.perform(get("/events/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("POST /events - Should handle event with coherent dates (end after start)")
    void shouldHandleEventWithCoherentDates() throws Exception {
        // Given
        Event event = new Event();
        event.setName("Multi-day Event");
        event.setStartDate(LocalDate.of(2025, 12, 1));
        event.setEndDate(LocalDate.of(2025, 12, 10));
        event.setCreatorId(creatorId);

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate", is("2025-12-01")))
                .andExpect(jsonPath("$.endDate", is("2025-12-10")));
    }

    @Test
    @DisplayName("POST /events - Should accept same day event (start equals end)")
    void shouldAcceptSameDayEvent() throws Exception {
        // Given
        LocalDate eventDate = LocalDate.of(2025, 12, 1);
        Event event = new Event();
        event.setName("Same Day Event");
        event.setStartDate(eventDate);
        event.setEndDate(eventDate);
        event.setCreatorId(creatorId);

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDate", is("2025-12-01")))
                .andExpect(jsonPath("$.endDate", is("2025-12-01")));
    }

    @Test
    @DisplayName("POST /events - Should set default creation date if not provided")
    void shouldSetDefaultCreationDate() throws Exception {
        // Given
        Event event = new Event();
        event.setName("Event with auto creation date");
        event.setStartDate(LocalDate.now().plusDays(10));
        event.setEndDate(LocalDate.now().plusDays(12));
        event.setCreatorId(creatorId);
        // Not setting creationDate

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creationDate", is(LocalDate.now().toString())));
    }

    @Test
    @DisplayName("POST /events - Should set default status to active if not provided")
    void shouldSetDefaultStatusToActive() throws Exception {
        // Given
        Event event = new Event();
        event.setName("Event with default status");
        event.setStartDate(LocalDate.now().plusDays(10));
        event.setEndDate(LocalDate.now().plusDays(12));
        event.setCreatorId(creatorId);
        // Not setting status

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("active")));
    }

    @Test
    @DisplayName("PATCH /events/{id}/status - Should transition event through all statuses")
    void shouldTransitionEventThroughAllStatuses() throws Exception {
        // Given
        Event event = createTestEvent("Test Event", EventStatus.active);

        // When & Then - Active to Full
        mockMvc.perform(patch("/events/{id}/status", event.getEventId())
                        .param("status", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("full")));

        // Full to Canceled
        mockMvc.perform(patch("/events/{id}/status", event.getEventId())
                        .param("status", "canceled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("canceled")));
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
        return eventRepository.save(event);
    }
}

