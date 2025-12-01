package app.quantun.architecture.presentation.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for shipping address data in HTTP requests.
 */
public record ShippingAddressRequest(
        @NotBlank @Size(max = 200) String street,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String zipCode,
        @NotBlank @Size(max = 100) String country
) {}
