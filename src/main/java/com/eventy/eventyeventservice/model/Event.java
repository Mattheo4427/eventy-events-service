package com.eventy.eventyeventservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id", columnDefinition = "uuid")
    private UUID eventId;

    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name cannot exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "location")
    private String location;

    @Column(name = "full_address")
    private String fullAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_type_id")
    private EventType eventType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private EventCategory category;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Event status is required")
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.active;

    @NotNull(message = "Creator ID is required")
    @Column(name = "creator_id", nullable = false, columnDefinition = "uuid")
    private UUID creatorId;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate = LocalDate.now();
}

