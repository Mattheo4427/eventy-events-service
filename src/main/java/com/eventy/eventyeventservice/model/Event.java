package com.eventy.eventyeventservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "event")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id", columnDefinition = "uuid")
    private UUID eventId;

    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name cannot exceed 255 characters")
    @Column(name = "name", nullable = false, columnDefinition = "varchar(255)")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false, columnDefinition = "date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false, columnDefinition = "date")
    private LocalDate endDate;

    @Column(name = "location", columnDefinition = "varchar(255)")
    private String location;

    @Column(name = "full_address", columnDefinition = "varchar(255)")
    private String fullAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_type_id", columnDefinition = "uuid")
    private EventType eventType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", columnDefinition = "uuid")
    private EventCategory category;

    @Column(name = "image_url", columnDefinition = "varchar(512)")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Event status is required")
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50)")
    private EventStatus status = EventStatus.active;

    @NotNull(message = "Creator ID is required")
    @Column(name = "creator_id", nullable = false, columnDefinition = "uuid")
    private UUID creatorId;

    @Column(name = "creation_date", nullable = false, updatable = false, columnDefinition = "date")
    private LocalDate creationDate = LocalDate.now();
}

