package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.dto.OrderCreateRequest;
import app.quantun.architecture.adapter.in.web.dto.OrderResponse;
import app.quantun.architecture.adapter.in.web.mapper.OrderWebMapper;
import app.quantun.architecture.application.port.in.CreateOrderCommand;
import app.quantun.architecture.application.port.in.CreateOrderUseCase;
import app.quantun.architecture.domain.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Order processing operations")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderWebMapper mapper;

    @Operation(summary = "Create a new order")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        CreateOrderCommand command = mapper.toCommand(request);
        Order order = createOrderUseCase.createOrder(command);
        OrderResponse response = mapper.toResponse(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
