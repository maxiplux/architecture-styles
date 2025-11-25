package app.quantun.architecture.adapter.in.web;

import app.quantun.architecture.adapter.in.web.dto.*;
import app.quantun.architecture.adapter.in.web.mapper.OrderWebMapper;
import app.quantun.architecture.application.port.in.CreateOrderCommand;
import app.quantun.architecture.application.port.in.CreateOrderUseCase;
import app.quantun.architecture.domain.model.Order;
import app.quantun.architecture.domain.model.OrderItem;
import app.quantun.architecture.domain.model.OrderStatus;
import app.quantun.architecture.domain.model.ShippingAddress;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateOrderUseCase createOrderUseCase;
    @MockBean private OrderWebMapper orderWebMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(5001L);
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(101L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));
        ShippingAddressRequest address = new ShippingAddressRequest();
        address.setStreet("123 Main St");
        address.setCity("City");
        address.setState("ST");
        address.setZipCode("12345");
        address.setCountry("USA");
        request.setShippingAddress(address);

        OrderResponse response = new OrderResponse(
            78001L, "PENDING", List.of(), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, OffsetDateTime.now()
        );

        CreateOrderCommand command = new CreateOrderCommand(1L, List.of(), null);
        ShippingAddress shippingAddress = new ShippingAddress("Street", "City", "State", "Zip", "Country");
        List<OrderItem> items = new ArrayList<>(); 

        Order createdOrder = new Order(
            78001L, 
            5001L, 
            OrderStatus.PENDING, 
            BigDecimal.TEN, 
            BigDecimal.ONE, 
            BigDecimal.TEN, 
            OffsetDateTime.now(), 
            shippingAddress, 
            items
        );

        when(orderWebMapper.toCommand(any())).thenReturn(command);
        when(createOrderUseCase.createOrder(any())).thenReturn(createdOrder);
        when(orderWebMapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(78001));
    }
}
