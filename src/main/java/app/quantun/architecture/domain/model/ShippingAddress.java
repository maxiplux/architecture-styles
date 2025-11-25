package app.quantun.architecture.domain.model;

public record ShippingAddress(
    String street,
    String city,
    String state,
    String zipCode,
    String country
) {
    public ShippingAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be blank");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code cannot be blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be blank");
        }
    }
}