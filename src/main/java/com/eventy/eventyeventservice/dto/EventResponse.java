package com.eventy.eventyeventservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class EventResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String fullAddress;
    private String imageUrl;
    private String status;
    private String typeLabel;      // Label du type (ex: Concert)
    private String categoryLabel;  // Label de la catégorie (ex: Musique)
    private UUID creatorId;
}