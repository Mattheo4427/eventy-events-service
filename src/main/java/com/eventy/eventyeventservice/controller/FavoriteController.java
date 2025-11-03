package com.eventy.eventyeventservice.controller;

import com.eventy.eventyeventservice.model.Favorite;
import com.eventy.eventyeventservice.repository.FavoriteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for favorite management
 */
@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;

    public FavoriteController(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Get all favorites for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getFavoritesByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(favoriteRepository.findByUserId(userId));
    }

    /**
     * Get a favorite by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Favorite> getFavoriteById(@PathVariable UUID id) {
        return favoriteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add an event to favorites
     */
    @PostMapping
    public ResponseEntity<Favorite> createFavorite(@Valid @RequestBody Favorite favorite) {
        if (favorite.getAddedDate() == null) {
            favorite.setAddedDate(LocalDate.now());
        }
        Favorite savedFavorite = favoriteRepository.save(favorite);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFavorite);
    }

    /**
     * Remove a favorite by its ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable UUID id) {
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Remove an event from a user's favorites
     */
    @DeleteMapping("/user/{userId}/event/{eventId}")
    @Transactional
    public ResponseEntity<Void> deleteFavoriteByUserAndEvent(@PathVariable UUID userId, @PathVariable UUID eventId) {
        favoriteRepository.findByUserIdAndEvent_EventId(userId, eventId)
                .ifPresent(favoriteRepository::delete);
        return ResponseEntity.noContent().build();
    }
}

