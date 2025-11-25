package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull
    @Schema(description = "ID of the customer placing the order", example = "1")
    private Long customerId;

    @NotEmpty
    @Valid
    @Schema(description = "List of items to order")
    private List<OrderItemRequest> items;

    @NotNull
    @Valid
    @Schema(description = "Shipping address for the order")
    private ShippingAddressRequest shippingAddress;
}