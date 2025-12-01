package app.quantun.architecture.presentation.rest.dto.response;

import java.util.List;

/**
 * Generic response model for paginated data in HTTP responses.
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {}
