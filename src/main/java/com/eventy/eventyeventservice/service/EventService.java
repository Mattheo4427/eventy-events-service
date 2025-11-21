package com.eventy.eventyeventservice.service;

import com.eventy.eventyeventservice.dto.EventRequest;
import com.eventy.eventyeventservice.dto.EventResponse;
import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final EventCategoryRepository categoryRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        // Validation basique des dépendances
        EventType type = null;
        if(request.getEventTypeId() != null) {
            type = eventTypeRepository.findById(request.getEventTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Type not found"));
        }

        EventCategory category = null;
        if(request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        }

        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDate(LocalDate.from(request.getStartDate()));
        event.setEndDate(LocalDate.from(request.getEndDate()));
        event.setLocation(request.getLocation());
        event.setFullAddress(request.getFullAddress());
        event.setImageUrl(request.getImageUrl());
        event.setStatus(EventStatus.active); // Statut par défaut
        event.setCreatorId(request.getCreatorId());
        event.setCreationDate(LocalDate.from(LocalDateTime.now()));
        event.setEventType(type);
        event.setCategory(category);

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String keyword) {
        return eventRepository.searchByKeyword(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));
        return mapToResponse(event);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with ID: " + id);
        }
        eventRepository.deleteById(id);
    }

    // Mapper utilitaire (pourrait être remplacé par MapStruct)
    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getEventId())
                .name(event.getName())
                .description(event.getDescription())
                .startDate(event.getStartDate().atStartOfDay())
                .endDate(event.getEndDate().atStartOfDay())
                .location(event.getLocation())
                .fullAddress(event.getFullAddress())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus() != null ? event.getStatus().name() : "UNKNOWN")
                .typeLabel(event.getEventType() != null ? event.getEventType().getLabel() : null)
                .categoryLabel(event.getCategory() != null ? event.getCategory().getLabel() : null)
                .creatorId(event.getCreatorId())
                .build();
    }
}