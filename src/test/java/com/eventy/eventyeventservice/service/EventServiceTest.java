package com.eventy.eventyeventservice.service;

import com.eventy.eventyeventservice.dto.EventRequest;
import com.eventy.eventyeventservice.dto.EventResponse;
import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventTypeRepository eventTypeRepository;
    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Should create event successfully")
    void createEvent_ShouldReturnEventResponse() {
        // Arrange
        UUID typeId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // Request DTO
        EventRequest request = EventRequest.builder()
                .name("Concert Test")
                .eventTypeId(typeId)
                .categoryId(catId)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Paris")
                .build();

        // Mock Entities
        EventType type = new EventType();
        type.setEventTypeId(typeId);
        type.setLabel("Concert");

        EventCategory category = new EventCategory();
        category.setCategoryId(catId);
        category.setLabel("Music");

        // L'objet sauvegardé par le repository
        Event savedEvent = Event.builder()
                .eventId(eventId)
                .name("Concert Test")
                .eventType(type)
                .category(category)
                .status(EventStatus.active)
                .startDate(LocalDate.from(request.getStartDate()))
                .endDate(LocalDate.from(request.getEndDate()))
                .location("Paris")
                .build();

        // Mocking behavior
        when(eventTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(eventCategoryRepository.findById(catId)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Act
        EventResponse response = eventService.createEvent(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getName()).isEqualTo("Concert Test");
        assertThat(response.getTypeLabel()).isEqualTo("Concert");
        assertThat(response.getCategoryLabel()).isEqualTo("Music");

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should return event by ID when found")
    void getEventById_ShouldReturnResponse_WhenFound() {
        // Arrange
        UUID id = UUID.randomUUID();

        EventType type = new EventType();
        type.setLabel("Concert");
        EventCategory category = new EventCategory();
        category.setLabel("Music");

        Event event = Event.builder()
                .eventId(id)
                .name("Event Found")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .eventType(type)
                .category(category)
                .status(EventStatus.active)
                .build();

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        // Act
        EventResponse response = eventService.getEventById(id);

        // Assert
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Event Found");
    }

    @Test
    @DisplayName("Should throw exception when event ID not found")
    void getEventById_ShouldThrowException_WhenNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> eventService.getEventById(id));
    }

    @Test
    @DisplayName("Should filter events using getAvailableEvents")
    void getAvailableEvents_ShouldReturnFilteredList() {
        // Arrange
        String search = "Jazz";
        String location = "Paris";
        UUID catId = UUID.randomUUID();

        EventType type = new EventType();
        type.setLabel("Concert");
        EventCategory category = new EventCategory();
        category.setLabel("Music");

        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .name("Jazz Night")
                .location("Paris")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .eventType(type)
                .category(category)
                .status(EventStatus.active)
                .build();

        // On mock l'appel au repository avec les paramètres exacts
        when(eventRepository.searchEvents(search, location, catId))
                .thenReturn(List.of(event));

        // Act
        List<EventResponse> results = eventService.getAvailableEvents(search, location, catId);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Jazz Night");

        // Vérifie que le service passe bien les bons arguments au repo
        verify(eventRepository).searchEvents(search, location, catId);
    }

    @Test
    @DisplayName("Should update event successfully")
    void updateEvent_ShouldUpdateFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID newTypeId = UUID.randomUUID();

        // Request avec nouvelles valeurs
        EventRequest request = EventRequest.builder()
                .name("Updated Name")
                .eventTypeId(newTypeId)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .location("Lyon")
                .build();

        EventType newType = new EventType();
        newType.setEventTypeId(newTypeId);
        newType.setLabel("New Type");

        // Evénement existant
        Event existingEvent = Event.builder()
                .eventId(id)
                .name("Old Name")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .eventType(new EventType())
                .status(EventStatus.active)
                .build();

        when(eventRepository.findById(id)).thenReturn(Optional.of(existingEvent));
        when(eventTypeRepository.findById(newTypeId)).thenReturn(Optional.of(newType));

        // Mock du save : on retourne l'objet modifié (qui est le premier argument)
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EventResponse response = eventService.updateEvent(id, request);

        // Assert
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getTypeLabel()).isEqualTo("New Type");
        verify(eventRepository).save(existingEvent);
    }

    @Test
    @DisplayName("Should delete event when exists")
    void deleteEvent_ShouldCallRepository_WhenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(eventRepository.existsById(id)).thenReturn(true);

        // Act
        eventService.deleteEvent(id);

        // Assert
        verify(eventRepository).deleteById(id);
    }
}