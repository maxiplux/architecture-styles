package app.quantun.architecture.usecase.gateway;

import java.util.List;

/**
 * Generic container for paginated results from gateway operations.
 */
public record PagedResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
    public boolean isFirst() {
        return number == 0;
    }

    public boolean isLast() {
        return number >= totalPages - 1;
    }
}
