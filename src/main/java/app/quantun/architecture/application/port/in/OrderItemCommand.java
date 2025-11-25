package app.quantun.architecture.application.port.in;

public record OrderItemCommand(Long productId, Integer quantity) {}