package com.example.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OrderServiceApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context.getBean(OrderServiceApplication.class));
    }
}
