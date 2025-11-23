package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.dto.EventRequest;
import com.eventy.eventyeventservice.dto.EventResponse;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEvent_ShouldReturnCreated() throws Exception {
        // Arrange
        EventRequest request = EventRequest.builder()
                .name("New Event")
                .location("Lyon")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .eventTypeId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .build();

        EventResponse response = EventResponse.builder()
                .id(UUID.randomUUID())
                .name("New Event")
                .status("ACTIVE")
                .build();

        when(eventService.createEvent(any(EventRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Event"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getAllEvents_ShouldReturnList() throws Exception {
        // Arrange
        when(eventService.getAllEvents()).thenReturn(List.of(
                EventResponse.builder().name("Event 1").build(),
                EventResponse.builder().name("Event 2").build()
        ));

        // Act & Assert
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Event 1"));
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        EventResponse response = EventResponse.builder().id(id).name("My Event").build();
        when(eventService.getEventById(id)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Event"));
    }

    @Test
    void searchEvents_ShouldReturnFilteredList() throws Exception {
        // Arrange
        String keyword = "Jazz";
        when(eventService.searchEvents(keyword)).thenReturn(List.of(
                EventResponse.builder().name("Jazz Night").build()
        ));

        // Act & Assert
        mockMvc.perform(get("/events").param("search", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jazz Night"));
    }

    @Test
    void updateEventStatus_ShouldReturnUpdatedEvent() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        EventResponse response = EventResponse.builder().id(id).status("CANCELLED").build();

        when(eventService.updateEventStatus(eq(id), eq(EventStatus.canceled))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/events/{id}/status", id)
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deleteEvent_ShouldReturnNoContent() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        doNothing().when(eventService).deleteEvent(id);

        // Act & Assert
        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNoContent());
    }
}