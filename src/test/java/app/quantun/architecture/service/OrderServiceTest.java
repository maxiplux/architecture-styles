package app.quantun.architecture.service;

import app.quantun.architecture.domain.CustomerOrder;
import app.quantun.architecture.domain.Product;
import app.quantun.architecture.dto.order.OrderCreateRequest;
import app.quantun.architecture.dto.order.OrderItemRequest;
import app.quantun.architecture.dto.order.OrderResponse;
import app.quantun.architecture.dto.order.ShippingAddressDTO;
import app.quantun.architecture.mapper.OrderMapper;
import app.quantun.architecture.repository.CustomerOrderRepository;
import app.quantun.architecture.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(100L);
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        ShippingAddressDTO address = new ShippingAddressDTO();
        address.setStreet("Main St");
        address.setCity("City");
        address.setState("State");
        address.setZipCode("12345");
        address.setCountry("Country");
        request.setShippingAddress(address);

        Product product = Product.builder()
                .id(1L)
                .price(BigDecimal.TEN)
                .stock(100)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
             CustomerOrder order = invocation.getArgument(0);
             order.setId(999L);
             return order;
        });
        
        OrderResponse expectedResponse = new OrderResponse(999L, "PENDING", List.of(), BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, null);
        when(orderMapper.toResponse(any(CustomerOrder.class))).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
    }
}
