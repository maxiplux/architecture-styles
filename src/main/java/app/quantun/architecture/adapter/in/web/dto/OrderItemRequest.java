package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    @Schema(description = "ID of the product to order", example = "101")
    private Long productId;

    @NotNull
    @Min(1)
    @Max(99)
    @Schema(description = "Quantity of the product", example = "2", minimum = "1", maximum = "99")
    private Integer quantity;
}