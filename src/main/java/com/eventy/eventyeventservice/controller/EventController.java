package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.dto.EventResponse;
import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import com.eventy.eventyeventservice.repository.EventRepository;
import com.eventy.eventyeventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for event management
 */
@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Get all events
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * Get an event by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    /**
     * Get events by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getEventsByStatus(@PathVariable EventStatus status) {
        return ResponseEntity.ok(eventService.getByStatus(status));
    }

    /**
     * Get events created by a specific user
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<EventResponse>> getEventsByCreator(@PathVariable UUID creatorId) {
        return ResponseEntity.ok(eventRepository.findByCreatorId(creatorId));
    }

    /**
     * Get upcoming events
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        return ResponseEntity.ok(eventRepository.findByStartDateAfter(LocalDate.now()));
    }

    /**
     * Create a new event
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody Event event) {
        if (event.getCreationDate() == null) {
            event.setCreationDate(LocalDate.now());
        }
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.active);
        }
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
    }

    /**
     * Update an existing event
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable UUID id, @Valid @RequestBody Event event) {
        return eventRepository.findById(id)
                .map(existingEvent -> {
                    event.setEventId(id);
                    event.setCreationDate(existingEvent.getCreationDate());
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update event status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateEventStatus(@PathVariable UUID id, @RequestParam EventStatus status) {
        return eventRepository.findById(id)
                .map(event -> {
                    event.setStatus(status);
                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an event
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}



