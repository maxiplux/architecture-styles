package app.quantun.architecture.dto;

import java.time.OffsetDateTime;

public record CategoryDTO(
        Long id,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt
) {}
