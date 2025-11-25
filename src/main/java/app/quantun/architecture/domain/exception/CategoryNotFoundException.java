package app.quantun.architecture.domain.exception;

public class CategoryNotFoundException extends DomainException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}