package app.quantun.architecture.presentation.presenter;

import app.quantun.architecture.presentation.rest.dto.response.OrderItemResponse;
import app.quantun.architecture.presentation.rest.dto.response.OrderResponse;
import app.quantun.architecture.usecase.order.OrderItemOutputData;
import app.quantun.architecture.usecase.order.OrderOutputData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Presenter for transforming order use case output to HTTP response models.
 */
@Component
public class OrderPresenter {

    public OrderResponse present(OrderOutputData output) {
        List<OrderItemResponse> items = output.items().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                output.orderId(),
                output.status(),
                items,
                output.subtotal(),
                output.tax(),
                output.total(),
                output.createdAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItemOutputData item) {
        return new OrderItemResponse(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal()
        );
    }
}
