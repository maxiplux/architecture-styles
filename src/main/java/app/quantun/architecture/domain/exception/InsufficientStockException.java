package app.quantun.architecture.domain.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String message) {
        super(message);
    }
}