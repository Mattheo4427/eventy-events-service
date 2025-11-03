package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for favorite management
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    /**
     * Find all favorites for a specific user
     */
    List<Favorite> findByUserId(UUID userId);

    /**
     * Find a favorite by user and event
     */
    Optional<Favorite> findByUserIdAndEvent_EventId(UUID userId, UUID eventId);

    /**
     * Delete a favorite by user and event
     */
    void deleteByUserIdAndEvent_EventId(UUID userId, UUID eventId);
}



