package app.quantun.architecture.usecase.product;

import java.util.List;

/**
 * Output data for paginated product results.
 */
public record PagedProductOutputData(
        List<ProductOutputData> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {}
