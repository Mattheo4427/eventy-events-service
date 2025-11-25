package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.dto.EventRequest;
import com.eventy.eventyeventservice.dto.EventResponse;
import com.eventy.eventyeventservice.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// NOUVEAUX IMPORTS IMPORTANTS
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Remplace MockBean
import org.springframework.security.test.context.support.WithMockUser; // Pour simuler l'auth
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Pour le token CSRF

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // CORRECTION : @MockitoBean remplace @MockBean dans Spring Boot 3.4+
    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /events - Should pass no params when empty")
    @WithMockUser // Simule un utilisateur connecté
    void getAllEvents_NoParams_ShouldReturnList() throws Exception {
        // Arrange
        when(eventService.getAvailableEvents(null, null, null)).thenReturn(List.of(
                EventResponse.builder().name("Event 1").build(),
                EventResponse.builder().name("Event 2").build()
        ));

        // Act & Assert
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /events - Should pass query params to service")
    @WithMockUser
    void getAllEvents_WithParams_ShouldPassFilters() throws Exception {
        // Arrange
        String search = "Jazz";
        String location = "Paris";
        UUID catId = UUID.randomUUID();

        when(eventService.getAvailableEvents(eq(search), eq(location), eq(catId)))
                .thenReturn(List.of(EventResponse.builder().name("Jazz Event").build()));

        // Act & Assert
        mockMvc.perform(get("/events")
                        .param("search", search)
                        .param("location", location)
                        .param("categoryId", catId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jazz Event"));
    }

    @Test
    @DisplayName("POST /events - Should create event (Admin Role)")
    @WithMockUser(roles = "ADMIN") // Rôle ADMIN requis
    void createEvent_ShouldReturnCreated() throws Exception {
        // Arrange
        EventRequest request = EventRequest.builder()
                .name("New Event")
                .eventTypeId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .location("Lyon")
                .build();

        EventResponse response = EventResponse.builder()
                .id(UUID.randomUUID())
                .name("New Event")
                .status("ACTIVE")
                .build();

        when(eventService.createEvent(any(EventRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/events")
                        .with(csrf()) // AJOUT : Token CSRF requis pour les POST/PUT/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Event"));
    }

    @Test
    @DisplayName("GET /events/{id} - Should return event")
    @WithMockUser
    void getEventById_ShouldReturnEvent() throws Exception {
        UUID id = UUID.randomUUID();
        EventResponse response = EventResponse.builder().id(id).name("My Event").build();

        when(eventService.getEventById(id)).thenReturn(response);

        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Event"));
    }

    @Test
    @DisplayName("DELETE /events/{id} - Should delete (Admin Role)")
    @WithMockUser(roles = "ADMIN")
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(eventService).deleteEvent(id);

        mockMvc.perform(delete("/events/{id}", id)
                        .with(csrf())) // AJOUT : Token CSRF requis
                .andExpect(status().isNoContent());
    }
}