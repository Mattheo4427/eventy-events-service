package com.eventy.eventyeventservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    private String fullAddress;
    private String imageUrl;

    private UUID eventTypeId;
    private UUID categoryId;

    // Le creatorId est souvent extrait du Token JWT, mais on peut le passer ici pour le MVP
    private UUID creatorId;
}