package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.model.Favorite;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
import com.eventy.eventyeventservice.repository.FavoriteRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Favorite Controller Integration Tests")
class FavoriteControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventTypeRepository eventTypeRepository;
    @Autowired private EventCategoryRepository eventCategoryRepository;

    private Event event1;
    private EventType defaultType;
    private EventCategory defaultCategory;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        eventRepository.deleteAll();
        // Nettoyer les types et catégories aussi pour éviter les contraintes d'unicité
        eventTypeRepository.deleteAll();
        eventCategoryRepository.deleteAll();

        // CORRECTION : Utilisation des setters au lieu du constructeur
        EventType type = new EventType();
        type.setLabel("Type Fav");
        defaultType = eventTypeRepository.save(type);

        EventCategory category = new EventCategory();
        category.setLabel("Cat Fav");
        defaultCategory = eventCategoryRepository.save(category);

        event1 = createEvent("Event for Fav");
    }

    @Test
    @WithMockUser
    @DisplayName("GET /favorites/user/{userId} - Should list favorites")
    void shouldReturnUserFavorites() throws Exception {
        UUID userId = UUID.randomUUID();
        createFavorite(userId, event1);

        mockMvc.perform(get("/favorites/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /favorites - Should add a favorite")
    void shouldAddFavorite() throws Exception {
        UUID userId = UUID.randomUUID();
        // Construction du JSON manuel pour éviter la dépendance DTO ici
        String json = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", userId, event1.getEventId());

        mockMvc.perform(post("/favorites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // Vérification en base
        mockMvc.perform(get("/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /favorites/... - Should remove favorite")
    void shouldRemoveFavorite() throws Exception {
        UUID userId = UUID.randomUUID();
        createFavorite(userId, event1);

        mockMvc.perform(delete("/favorites/user/{userId}/event/{eventId}", userId, event1.getEventId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/favorites/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Event createEvent(String name) {
        Event event = new Event();
        event.setName(name);
        event.setDescription("Desc");
        event.setStartDate(LocalDate.now().plusDays(1));
        event.setEndDate(LocalDate.now().plusDays(2));
        event.setLocation("Paris");
        event.setFullAddress("Address");
        event.setStatus(EventStatus.active);
        event.setCreatorId(UUID.randomUUID());
        event.setCreationDate(LocalDate.now());
        event.setEventType(defaultType);
        event.setCategory(defaultCategory);
        return eventRepository.save(event);
    }

    private void createFavorite(UUID userId, Event event) {
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setEvent(event);
        fav.setAddedDate(LocalDate.now());
        favoriteRepository.save(fav);
    }
}