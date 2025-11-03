package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for event type management
 */
@Repository
public interface EventTypeRepository extends JpaRepository<EventType, UUID> {
    /**
     * Find an event type by its label
     */
    Optional<EventType> findByLabel(String label);
}

