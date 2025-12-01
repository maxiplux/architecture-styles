package app.quantun.architecture.presentation.rest.dto.response;

import java.time.OffsetDateTime;

/**
 * Response model for category data in HTTP responses.
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt
) {}
