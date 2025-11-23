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
    void createEvent_ShouldReturnEventResponse() {
        // Arrange
        UUID typeId = UUID.randomUUID();
        UUID catId = UUID.randomUUID();

        // Request DTO
        EventRequest request = EventRequest.builder()
                .name("Concert Test")
                .eventTypeId(typeId)
                .categoryId(catId)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .location("Paris")
                .build();

        // Mock Entities
        EventType type = new EventType();
        type.setEventTypeId(typeId);
        type.setLabel("Concert");

        EventCategory category = new EventCategory();
        category.setCategoryId(catId);
        category.setLabel("Music");

        // L'objet qui sera "sauvegardé" et retourné par le mock
        Event savedEvent = Event.builder()
                .eventId(UUID.randomUUID())
                .name("Concert Test")
                .eventType(type)        // IMPORTANT : La relation doit être présente
                .category(category)     // IMPORTANT : La relation doit être présente
                .status(EventStatus.active)
                .startDate(LocalDate.from(request.getStartDate()))
                .endDate(LocalDate.from(request.getEndDate()))
                .build();

        // Mocking behavior
        when(eventTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(eventCategoryRepository.findById(catId)).thenReturn(Optional.of(category));
        // On utilise any() mais on retourne l'objet complet construit ci-dessus
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Act
        EventResponse response = eventService.createEvent(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Concert Test");
        assertThat(response.getTypeLabel()).isEqualTo("Concert"); // Vérifie que le mapping relationnel a fonctionné
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEventById_ShouldReturnResponse_WhenFound() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Création des objets liés pour éviter les NullPointerException dans le mapper
        EventType type = new EventType();
        type.setLabel("Concert");
        EventCategory category = new EventCategory();
        category.setLabel("Music");

        // On peuple TOUS les champs susceptibles d'être utilisés par le mapper
        Event event = Event.builder()
                .eventId(id) // ou .id(id) selon votre Lombok
                .name("Event Found")
                .description("Desc")
                .location("Paris")
                .startDate(LocalDate.from(LocalDateTime.now())) // CRUCIAL pour éviter NPE
                .endDate(LocalDate.from(LocalDateTime.now().plusHours(2))) // CRUCIAL
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
    void getEventById_ShouldThrowException_WhenNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> eventService.getEventById(id));
    }

    @Test
    void searchEvents_ShouldReturnList() {
        // Arrange
        String keyword = "Rock";

        EventType type = new EventType();
        type.setLabel("Concert");
        EventCategory category = new EventCategory();
        category.setLabel("Music");

        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .name("Rock Festival")
                .startDate(LocalDate.from(LocalDateTime.now())) // CRUCIAL
                .endDate(LocalDate.from(LocalDateTime.now().plusDays(1))) // CRUCIAL
                .eventType(type)
                .category(category)
                .status(EventStatus.active)
                .build();

        when(eventRepository.searchByKeyword(keyword)).thenReturn(List.of(event));

        // Act
        List<EventResponse> results = eventService.searchEvents(keyword);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Rock Festival");
    }

    @Test
    void updateEvent_ShouldUpdateFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID newTypeId = UUID.randomUUID();
        UUID catId = UUID.randomUUID(); // Nécessaire pour l'initialisation

        // Request avec les nouvelles valeurs
        EventRequest request = EventRequest.builder()
                .name("Updated Name")
                .eventTypeId(newTypeId)
                // On ajoute des dates car le service update va probablement les setter
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .location("Lyon")
                .build();

        // Mock des dépendances relationnelles
        EventType newType = new EventType();
        newType.setEventTypeId(newTypeId);
        newType.setLabel("New Type");

        EventCategory category = new EventCategory();
        category.setCategoryId(catId);
        category.setLabel("Music");

        // L'événement existant en BDD (COMPLET)
        Event existingEvent = Event.builder()
                .eventId(id)
                .name("Old Name")
                .startDate(LocalDate.from(LocalDateTime.now().minusDays(1)))
                .endDate(LocalDate.from(LocalDateTime.now()))
                .eventType(new EventType()) // Ancien type
                .category(category)
                .status(EventStatus.active)
                .build();

        when(eventRepository.findById(id)).thenReturn(Optional.of(existingEvent));
        when(eventTypeRepository.findById(newTypeId)).thenReturn(Optional.of(newType));
        // Mock du save qui retourne l'objet modifié
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        EventResponse response = eventService.updateEvent(id, request);

        // Assert
        assertThat(response.getName()).isEqualTo("Updated Name");
        // Vérifie que le repository a bien été appelé avec l'objet modifié
        verify(eventRepository).save(existingEvent);
    }

    @Test
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