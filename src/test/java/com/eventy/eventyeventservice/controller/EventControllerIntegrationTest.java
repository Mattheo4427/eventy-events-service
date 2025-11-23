package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.dto.EventRequest;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Annule les modifications en BDD après chaque test
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

    private UUID creatorId;
    private EventType testEventType;
    private EventCategory testEventCategory;



    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        eventTypeRepository.deleteAll();
        eventCategoryRepository.deleteAll();

        creatorId = UUID.randomUUID();

        // Création des données de référence (Types et Catégories) nécessaires pour les DTOs
        testEventType = new EventType();
        testEventType.setLabel("Concert");
        testEventType = eventTypeRepository.save(testEventType);

        testEventCategory = new EventCategory();
        testEventCategory.setLabel("Music");
        testEventCategory = eventCategoryRepository.save(testEventCategory);
    }

    @Test
    @DisplayName("POST /events - Should create a new event")
    void shouldCreateEvent() throws Exception {
        // Given : On utilise EventRequest (DTO) et non Event (Entité)
        EventRequest request = EventRequest.builder()
                .name("New Integration Event")
                .description("Description")
                .location("Paris")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .eventTypeId(testEventType.getEventTypeId()) // ID du type existant
                .categoryId(testEventCategory.getCategoryId()) // ID de la catégorie existante
                .creatorId(creatorId)
                .build();

        // When & Then
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue())) // Le DTO renvoie "id", pas "eventId"
                .andExpect(jsonPath("$.name", is("New Integration Event")))
                .andExpect(jsonPath("$.status", is("active")));
    }

    @Test
    @DisplayName("GET /events - Should return list of events")
    void shouldGetAllEvents() throws Exception {
        // Given
        createTestEvent("Event 1", EventStatus.active);
        createTestEvent("Event 2", EventStatus.active);

        // When & Then
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", notNullValue())); // Vérification du champ ID du DTO
    }

    @Test
    @DisplayName("GET /events/{id} - Should return event details")
    void shouldGetEventById() throws Exception {
        // Given
        Event event = createTestEvent("Detail Event", EventStatus.active);

        // When & Then
        mockMvc.perform(get("/events/{id}", event.getEventId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(event.getEventId().toString())))
                .andExpect(jsonPath("$.name", is("Detail Event")));
    }

    @Test
    @DisplayName("PUT /events/{id} - Should update event")
    void shouldUpdateEvent() throws Exception {
        // Given
        Event existingEvent = createTestEvent("Original Name", EventStatus.active);

        // Création du DTO de mise à jour
        EventRequest updateRequest = EventRequest.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .location("Lyon")
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(6))
                .eventTypeId(testEventType.getEventTypeId())
                .categoryId(testEventCategory.getCategoryId())
                .build();

        // When & Then
        mockMvc.perform(put("/events/{id}", existingEvent.getEventId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.location", is("Lyon")));
    }

    @Test
    @DisplayName("PATCH /events/{id}/status - Should update status")
    void shouldUpdateEventStatus() throws Exception {
        // Given
        Event event = createTestEvent("Status Event", EventStatus.active);

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
        Event event = createTestEvent("To Delete", EventStatus.active);

        // When & Then
        mockMvc.perform(delete("/events/{id}", event.getEventId()))
                .andExpect(status().isNoContent());

        // Verify
        mockMvc.perform(get("/events/{id}", event.getEventId()))
                .andExpect(status().isNotFound()); // Le service renvoie 404 si non trouvé (via EntityNotFoundException)
    }

    // --- Helper ---

    private Event createTestEvent(String name, EventStatus status) {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Test Description");
        event.setStartDate(LocalDate.from(LocalDateTime.now().plusDays(1)));
        event.setEndDate(LocalDate.from(LocalDateTime.now().plusDays(2)));
        event.setLocation("Paris");
        event.setFullAddress("123 Rue Test");
        event.setStatus(status);
        event.setCreatorId(creatorId);
        event.setCreationDate(LocalDate.from(LocalDateTime.now()));
        event.setEventType(testEventType);     // Relation obligatoire
        event.setCategory(testEventCategory);  // Relation obligatoire
        return eventRepository.save(event);
    }
}