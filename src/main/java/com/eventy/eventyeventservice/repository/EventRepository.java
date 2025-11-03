package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for event management
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    /**
     * Find all events by status
     */
    List<Event> findByStatus(EventStatus status);

    /**
     * Find all events created by a specific user
     */
    List<Event> findByCreatorId(UUID creatorId);

    /**
     * Find all events starting after a given date
     */
    List<Event> findByStartDateAfter(LocalDate date);
}

