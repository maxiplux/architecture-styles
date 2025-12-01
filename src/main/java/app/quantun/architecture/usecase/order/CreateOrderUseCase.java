package app.quantun.architecture.usecase.order;

/**
 * Input boundary (use case interface) for creating orders.
 */
public interface CreateOrderUseCase {
    OrderOutputData execute(CreateOrderInputData input);
}
