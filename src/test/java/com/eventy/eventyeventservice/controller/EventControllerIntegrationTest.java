package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Event Controller Integration Tests")
class EventControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventTypeRepository eventTypeRepository;
    @Autowired private EventCategoryRepository eventCategoryRepository;

    private EventType concertType;
    private EventCategory musicCategory;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        // Nettoyage avant chaque test
        eventRepository.deleteAll();
        eventTypeRepository.deleteAll();
        eventCategoryRepository.deleteAll();

        // Création des données référentielles
        concertType = new EventType();
        concertType.setLabel("Concert");
        concertType = eventTypeRepository.save(concertType);

        musicCategory = new EventCategory();
        musicCategory.setLabel("Music");
        musicCategory = eventCategoryRepository.save(musicCategory);

        creatorId = UUID.randomUUID();
    }

    @Test
    @WithMockUser
    @DisplayName("Integration: Search with location filter")
    void shouldFilterByLocation() throws Exception {
        // Given
        createEvent("Jazz Festival", "Paris", musicCategory);
        createEvent("Rock Concert", "Lyon", musicCategory);

        // When & Then
        mockMvc.perform(get("/events")
                        .param("location", "Paris")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Jazz Festival")))
                .andExpect(jsonPath("$[0].location", is("Paris")));
    }

    @Test
    @WithMockUser
    @DisplayName("Integration: Search with keyword")
    void shouldFilterByKeyword() throws Exception {
        // Given
        createEvent("Jazz Night", "Paris", musicCategory);
        createEvent("Techno Party", "Paris", musicCategory);

        // When & Then
        mockMvc.perform(get("/events")
                        .param("search", "Jazz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Jazz Night")));
    }

    @Test
    @WithMockUser
    @DisplayName("Integration: Search with category")
    void shouldFilterByCategory() throws Exception {
        // Given
        EventCategory sportCategory = new EventCategory();
        sportCategory.setLabel("Sport");
        sportCategory = eventCategoryRepository.save(sportCategory);

        createEvent("Concert A", "Paris", musicCategory);
        createEvent("Match B", "Paris", sportCategory);

        // When & Then
        mockMvc.perform(get("/events")
                        .param("categoryId", musicCategory.getCategoryId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Concert A")));
    }

    private void createEvent(String name, String location, EventCategory category) {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Description");
        event.setStartDate(LocalDate.now().plusDays(1));
        event.setEndDate(LocalDate.now().plusDays(2));
        event.setLocation(location);
        event.setFullAddress("123 Rue " + location);
        event.setEventType(concertType);
        event.setCategory(category);
        event.setStatus(EventStatus.active);
        event.setCreatorId(creatorId);
        event.setCreationDate(LocalDate.now());
        eventRepository.save(event);
    }
}