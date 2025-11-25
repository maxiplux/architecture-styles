package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
    @Schema(description = "Unique identifier of the order", example = "1001")
    Long orderId,

    @Schema(description = "Status of the order", example = "PENDING")
    String status,

    @Schema(description = "List of items in the order")
    List<OrderItemResponse> items,

    @Schema(description = "Subtotal amount before tax", example = "99.99")
    BigDecimal subtotal,

    @Schema(description = "Tax amount", example = "8.00")
    BigDecimal tax,

    @Schema(description = "Total amount including tax", example = "107.99")
    BigDecimal total,

    @Schema(description = "Timestamp when the order was created", example = "2023-11-24T10:00:00Z")
    OffsetDateTime createdAt
) {}