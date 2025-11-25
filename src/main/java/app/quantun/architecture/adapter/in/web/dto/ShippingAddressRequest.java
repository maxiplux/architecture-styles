package app.quantun.architecture.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShippingAddressRequest {
    @NotBlank
    @Size(max = 200)
    @Schema(description = "Street address", example = "123 Main St")
    private String street;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "City", example = "New York")
    private String city;

    @Size(max = 100)
    @Schema(description = "State or province", example = "NY")
    private String state;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "Zip or postal code", example = "10001")
    private String zipCode;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Country", example = "USA")
    private String country;
}