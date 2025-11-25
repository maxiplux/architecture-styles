package app.quantun.architecture.domain.exception;

public class ProductNotActiveException extends DomainException {
    public ProductNotActiveException(String message) {
        super(message);
    }
}