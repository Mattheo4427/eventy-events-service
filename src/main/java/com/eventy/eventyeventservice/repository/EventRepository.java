package com.eventy.eventyeventservice.repository;

import com.eventy.eventyeventservice.model.Event;
import com.eventy.eventyeventservice.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for event management
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
    *Recherche par mot clé dans le nom ou la description (insensible à la casse)
     */
    @Query("SELECT e FROM Event e WHERE LOWER(e.name)LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(@Param("keyword") String keyword);

    /**
    *  Filtre par lieu (ville)
     */
    List<Event> findByLocationContainingIgnoreCase(String location);

    /**
     Filtre par date (événements à venir)
     */
    List<Event> findByStartDateAfter(LocalDateTime date);

    /**
     Filtre par type d'événement
     */
    List<Event> findByEventTypeEventTypeId(UUID eventTypeId);

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

    @Query("SELECT e FROM Event e WHERE " +
           "(:keyword IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(CAST(e.description AS string)) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
           "(:location IS NULL OR LOWER(CAST(e.location AS string)) LIKE LOWER(CONCAT('%', CAST(:location AS string), '%'))) AND " +
           "(:categoryId IS NULL OR e.category.categoryId = :categoryId) AND " +
           "e.status = 'active'")
    List<Event> searchEvents(
        @Param("keyword") String keyword, 
        @Param("location") String location, 
        @Param("categoryId") UUID categoryId
    );

}

