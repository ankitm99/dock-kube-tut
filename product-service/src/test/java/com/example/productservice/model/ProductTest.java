package com.example.productservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductTest {

    @Test
    void constructorsAndAccessorsPreserveProductFields() {
        Product product = new Product(11L, "Desk", "Wooden desk", 125.5);

        assertEquals(11L, product.getId());
        assertEquals("Desk", product.getName());
        assertEquals("Wooden desk", product.getDescription());
        assertEquals(125.5, product.getPrice());

        product.setId(12L);
        product.setName("Chair");
        product.setDescription("Office chair");
        product.setPrice(75.0);

        assertEquals(12L, product.getId());
        assertEquals("Chair", product.getName());
        assertEquals("Office chair", product.getDescription());
        assertEquals(75.0, product.getPrice());
    }

    @Test
    void convenienceAndNoArgsConstructorsAreAvailable() {
        Product product = new Product("Keyboard", "Mechanical", 89.99);

        assertNull(product.getId());
        assertEquals("Keyboard", product.getName());
        assertEquals("Mechanical", product.getDescription());
        assertEquals(89.99, product.getPrice());
        assertNotNull(new Product());
    }
}
