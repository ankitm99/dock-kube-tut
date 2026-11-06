package com.example.orderservice.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void constructorsAndAccessorsPreserveOrderFields() {
        Instant createdAt = Instant.parse("2026-01-02T03:04:05Z");
        Order order = new Order(11L, 22L, "Desk", 4, 125.5, 502.0, "CONFIRMED", createdAt);

        assertEquals(11L, order.getId());
        assertEquals(22L, order.getProductId());
        assertEquals("Desk", order.getProductName());
        assertEquals(4, order.getQuantity());
        assertEquals(125.5, order.getUnitPrice());
        assertEquals(502.0, order.getTotalPrice());
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals(createdAt, order.getCreatedAt());

        Instant updatedAt = createdAt.plusSeconds(60);
        order.setId(12L);
        order.setProductId(23L);
        order.setProductName("Chair");
        order.setQuantity(2);
        order.setUnitPrice(75.0);
        order.setTotalPrice(150.0);
        order.setStatus("PROCESSING");
        order.setCreatedAt(updatedAt);

        assertEquals(12L, order.getId());
        assertEquals(23L, order.getProductId());
        assertEquals("Chair", order.getProductName());
        assertEquals(2, order.getQuantity());
        assertEquals(75.0, order.getUnitPrice());
        assertEquals(150.0, order.getTotalPrice());
        assertEquals("PROCESSING", order.getStatus());
        assertEquals(updatedAt, order.getCreatedAt());
    }

    @Test
    void convenienceConstructorAndNoArgsConstructorAreAvailable() {
        Order order = new Order(22L, "Desk", 1, 125.5, 125.5, "CONFIRMED", null);

        assertNull(order.getId());
        assertEquals(22L, order.getProductId());
        assertNotNull(new Order());
    }
}
