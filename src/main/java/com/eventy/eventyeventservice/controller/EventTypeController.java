package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.EventType;
import com.eventy.eventyeventservice.repository.EventTypeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for event type management
 */
@RestController
@RequestMapping("/event-types")
@CrossOrigin(origins = "*")
public class EventTypeController {

    private final EventTypeRepository eventTypeRepository;

    public EventTypeController(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    /**
     * Get all event types
     */
    @GetMapping
    public ResponseEntity<List<EventType>> getAllEventTypes() {
        return ResponseEntity.ok(eventTypeRepository.findAll());
    }

    /**
     * Get an event type by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventType> getEventTypeById(@PathVariable UUID id) {
        return eventTypeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new event type
     */
    @PostMapping
    public ResponseEntity<EventType> createEventType(@Valid @RequestBody EventType eventType) {
        EventType savedEventType = eventTypeRepository.save(eventType);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEventType);
    }

    /**
     * Update an existing event type
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventType> updateEventType(@PathVariable UUID id, @Valid @RequestBody EventType eventType) {
        return eventTypeRepository.findById(id)
                .map(existing -> {
                    eventType.setEventTypeId(id);
                    return ResponseEntity.ok(eventTypeRepository.save(eventType));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an event type
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventType(@PathVariable UUID id) {
        if (eventTypeRepository.existsById(id)) {
            eventTypeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

