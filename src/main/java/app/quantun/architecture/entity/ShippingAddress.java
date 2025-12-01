package app.quantun.architecture.entity;

/**
 * Value Object representing a shipping address.
 * Immutable record with validation in the compact constructor.
 */
public record ShippingAddress(
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
    public ShippingAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code is required");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
    }
}
