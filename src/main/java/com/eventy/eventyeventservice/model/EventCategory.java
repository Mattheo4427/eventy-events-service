package com.eventy.eventyeventservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "event_category")
@Data
@NoArgsConstructor
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id", columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "label", nullable = false, unique = true)
    private String label;
}

