package app.quantun.architecture.shared.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
