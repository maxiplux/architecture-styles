package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderItemResponse(
    @Schema(description = "ID of the product", example = "101")
    Long productId,

    @Schema(description = "Name of the product", example = "Smartphone")
    String productName,

    @Schema(description = "Quantity ordered", example = "2")
    Integer quantity,

    @Schema(description = "Price per unit", example = "999.99")
    BigDecimal unitPrice,

    @Schema(description = "Subtotal for this item", example = "1999.98")
    BigDecimal subtotal
) {}