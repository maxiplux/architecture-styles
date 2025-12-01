package app.quantun.architecture.presentation.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request model for order creation in HTTP requests.
 */
public record OrderCreateRequest(
        @NotNull Long customerId,
        @NotEmpty @Valid List<OrderItemRequest> items,
        @NotNull @Valid ShippingAddressRequest shippingAddress
) {}
