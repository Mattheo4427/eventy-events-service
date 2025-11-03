package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for event category management
 */
@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, UUID> {
    /**
     * Find a category by its label
     */
    Optional<EventCategory> findByLabel(String label);
}

