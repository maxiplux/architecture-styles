package app.quantun.architecture.domain.exception;

public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}