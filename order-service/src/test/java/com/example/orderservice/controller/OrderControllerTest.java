package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController controller;

    @Test
    void initDataCreatesSeedOrderWhenRepositoryIsEmpty() {
        when(orderRepository.count()).thenReturn(0L);

        controller.initData();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order seedOrder = orderCaptor.getValue();
        assertEquals(1L, seedOrder.getProductId());
        assertEquals("MacBook Pro", seedOrder.getProductName());
        assertEquals(1, seedOrder.getQuantity());
        assertEquals(1999.99, seedOrder.getUnitPrice());
        assertEquals(1999.99, seedOrder.getTotalPrice());
        assertEquals("CONFIRMED", seedOrder.getStatus());
        assertNotNull(seedOrder.getCreatedAt());
    }

    @Test
    void initDataDoesNotCreateSeedOrderWhenRepositoryIsNotEmpty() {
        when(orderRepository.count()).thenReturn(1L);

        controller.initData();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrdersReturnsRepositoryResults() {
        List<Order> orders = List.of(new Order(2L, "Keyboard", 1, 25.0, 25.0, "CONFIRMED", null));
        when(orderRepository.findAll()).thenReturn(orders);

        assertSame(orders, controller.getAllOrders());
    }

    @Test
    void getOrderByIdReturnsOrderWhenFound() {
        Order order = new Order(7L, "Mouse", 2, 10.0, 20.0, "CONFIRMED", null);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        ResponseEntity<Order> response = controller.getOrderById(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(order, response.getBody());
    }

    @Test
    void getOrderByIdReturnsNotFoundWhenMissing() {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseEntity<Order> response = controller.getOrderById(7L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createOrderRejectsNonPositiveQuantity() {
        ResponseEntity<?> response = controller.createOrder(new OrderRequest(5L, 0));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Quantity must be greater than 0", ((java.util.Map<?, ?>) response.getBody()).get("error"));
        verifyNoInteractions(productClient, orderRepository);
    }

    @Test
    void createOrderRejectsUnknownProduct() {
        when(productClient.getProductById(5L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.createOrder(new OrderRequest(5L, 2));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Product not found with id: 5", ((java.util.Map<?, ?>) response.getBody()).get("error"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderSavesAndReturnsConfirmedOrder() {
        ProductDTO product = new ProductDTO(5L, "Headphones", "Wireless", 49.99);
        when(productClient.getProductById(5L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = controller.createOrder(new OrderRequest(5L, 3));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order savedOrder = (Order) response.getBody();
        assertNotNull(savedOrder);
        assertEquals(5L, savedOrder.getProductId());
        assertEquals("Headphones", savedOrder.getProductName());
        assertEquals(3, savedOrder.getQuantity());
        assertEquals(49.99, savedOrder.getUnitPrice());
        assertEquals(149.97, savedOrder.getTotalPrice(), 0.000001);
        assertEquals("CONFIRMED", savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        verify(orderRepository).save(savedOrder);
    }
}
