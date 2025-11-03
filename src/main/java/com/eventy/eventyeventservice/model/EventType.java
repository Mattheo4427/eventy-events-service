package com.eventy.eventyeventservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "event_type")
@Data
@NoArgsConstructor
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_type_id", columnDefinition = "uuid")
    private UUID eventTypeId;

    @Column(name = "label", nullable = false, unique = true)
    private String label;
}

