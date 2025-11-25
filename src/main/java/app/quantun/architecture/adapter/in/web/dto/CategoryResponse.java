package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record CategoryResponse(
    @Schema(description = "Unique identifier of the category", example = "1")
    Long id,

    @Schema(description = "Name of the category", example = "Electronics")
    String name,

    @Schema(description = "Description of the category", example = "Electronic devices and gadgets")
    String description,

    @Schema(description = "Whether the category is active", example = "true")
    boolean active,

    @Schema(description = "Timestamp when the category was created", example = "2023-11-24T10:00:00Z")
    OffsetDateTime createdAt
) {}