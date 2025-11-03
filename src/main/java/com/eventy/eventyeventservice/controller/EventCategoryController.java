package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.EventCategory;
import com.eventy.eventyeventservice.repository.EventCategoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for event category management
 */
@RestController
@RequestMapping("/event-categories")
@CrossOrigin(origins = "*")
public class EventCategoryController {

    private final EventCategoryRepository eventCategoryRepository;

    public EventCategoryController(EventCategoryRepository eventCategoryRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
    }

    /**
     * Get all event categories
     */
    @GetMapping
    public ResponseEntity<List<EventCategory>> getAllEventCategories() {
        return ResponseEntity.ok(eventCategoryRepository.findAll());
    }

    /**
     * Get an event category by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventCategory> getEventCategoryById(@PathVariable UUID id) {
        return eventCategoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new event category
     */
    @PostMapping
    public ResponseEntity<EventCategory> createEventCategory(@Valid @RequestBody EventCategory eventCategory) {
        EventCategory savedEventCategory = eventCategoryRepository.save(eventCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEventCategory);
    }

    /**
     * Update an existing event category
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventCategory> updateEventCategory(@PathVariable UUID id, @Valid @RequestBody EventCategory eventCategory) {
        return eventCategoryRepository.findById(id)
                .map(existing -> {
                    eventCategory.setCategoryId(id);
                    return ResponseEntity.ok(eventCategoryRepository.save(eventCategory));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an event category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventCategory(@PathVariable UUID id) {
        if (eventCategoryRepository.existsById(id)) {
            eventCategoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

