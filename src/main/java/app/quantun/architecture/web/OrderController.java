package app.quantun.architecture.web;

import app.quantun.architecture.dto.order.OrderCreateRequest;
import app.quantun.architecture.dto.order.OrderResponse;
import app.quantun.architecture.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Order management and processing endpoints for creating and managing customer orders with items and shipping details")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

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
                                              "id": 1001,
                                              "orderNumber": "ORD-2025-001001",
                                              "customerId": 42,
                                              "status": "PENDING",
                                              "totalAmount": 749.98,
                                              "orderDate": "2025-11-24T16:36:00Z",
                                              "items": [
                                                {
                                                  "id": 2001,
                                                  "productId": 1,
                                                  "productName": "Wireless Bluetooth Headphones",
                                                  "quantity": 2,
                                                  "unitPrice": 149.99,
                                                  "subtotal": 299.98
                                                },
                                                {
                                                  "id": 2002,
                                                  "productId": 5,
                                                  "productName": "USB-C Charging Cable",
                                                  "quantity": 3,
                                                  "unitPrice": 150.00,
                                                  "subtotal": 450.00
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
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
