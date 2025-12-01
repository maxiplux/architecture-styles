package app.quantun.architecture.usecase.category;

import java.time.OffsetDateTime;

/**
 * Output data for category use cases.
 */
public record CategoryOutputData(
        Long id,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt
) {}
