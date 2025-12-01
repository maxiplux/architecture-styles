package app.quantun.architecture.presentation.rest.controller;

import app.quantun.architecture.presentation.rest.dto.request.OrderCreateRequest;
import app.quantun.architecture.presentation.rest.dto.response.OrderResponse;
import app.quantun.architecture.presentation.presenter.OrderPresenter;
import app.quantun.architecture.usecase.order.CreateOrderInputData;
import app.quantun.architecture.usecase.order.CreateOrderUseCase;
import app.quantun.architecture.usecase.order.OrderItemInputData;
import app.quantun.architecture.usecase.order.OrderOutputData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Order management and processing endpoints for creating and managing customer orders with items and shipping details")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderPresenter presenter;

    public OrderController(CreateOrderUseCase createOrderUseCase, OrderPresenter presenter) {
        this.createOrderUseCase = createOrderUseCase;
        this.presenter = presenter;
    }

    @Operation(
            summary = "Create a new order",
            description = "Creates a new customer order with specified items and shipping address. " +
                    "The order will be validated for product availability, stock quantities, and pricing. " +
                    "If successful, the order is persisted with PENDING status and a unique order number is generated. " +
                    "All items must reference valid, active products with sufficient stock. " +
                    "The total amount is automatically calculated based on product prices and quantities."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(
                                    name = "Order Created",
                                    summary = "Successfully created order with multiple items",
                                    value = """
                                            {
                                              "orderId": 1001,
                                              "status": "PENDING",
                                              "items": [
                                                {
                                                  "productId": 1,
                                                  "productName": "Wireless Bluetooth Headphones",
                                                  "quantity": 2,
                                                  "unitPrice": 149.99,
                                                  "subtotal": 299.98
                                                }
                                              ],
                                              "subtotal": 299.98,
                                              "tax": 24.00,
                                              "total": 323.98,
                                              "createdAt": "2025-11-24T16:36:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid order data, validation failed, or business rule violation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Validation Error",
                                            summary = "Missing required fields or invalid data format",
                                            value = """
                                                    {
                                                      "timestamp": "2025-11-24T16:36:00.000+00:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Validation failed: items must not be empty",
                                                      "path": "/api/v1/orders"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Insufficient Stock",
                                            summary = "Product doesn't have enough stock for requested quantity",
                                            value = """
                                                    {
                                                      "timestamp": "2025-11-24T16:36:00.000+00:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Insufficient stock for product ID 1. Available: 5, Requested: 10",
                                                      "path": "/api/v1/orders"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found - Product or customer doesn't exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Product with ID 999 not found",
                                              "path": "/api/v1/orders"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Product is inactive or unavailable",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Product ID 1 is not active and cannot be ordered",
                                              "path": "/api/v1/orders"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-11-24T16:36:00.000+00:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Unable to process order",
                                              "path": "/api/v1/orders"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestBody(
                    description = "Order creation request containing customer ID, list of order items with product IDs and quantities, and shipping address",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Order Request",
                                    summary = "Example order with multiple items and complete shipping address",
                                    value = """
                                            {
                                              "customerId": 42,
                                              "items": [
                                                {
                                                  "productId": 1,
                                                  "quantity": 2
                                                },
                                                {
                                                  "productId": 5,
                                                  "quantity": 3
                                                }
                                              ],
                                              "shippingAddress": {
                                                "street": "123 Main Street",
                                                "city": "New York",
                                                "state": "NY",
                                                "zipCode": "10001",
                                                "country": "USA"
                                              }
                                            }
                                            """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderCreateRequest request
    ) {
        // Convert request to use case input
        CreateOrderInputData input = new CreateOrderInputData(
                request.customerId(),
                request.items().stream()
                        .map(item -> new OrderItemInputData(item.productId(), item.quantity()))
                        .toList(),
                request.shippingAddress().street(),
                request.shippingAddress().city(),
                request.shippingAddress().state(),
                request.shippingAddress().zipCode(),
                request.shippingAddress().country()
        );

        // Execute use case
        OrderOutputData output = createOrderUseCase.execute(input);

        // Present result
        OrderResponse response = presenter.present(output);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
