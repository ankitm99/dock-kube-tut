package com.example.orderservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class OrderServiceApplicationMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            OrderServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(OrderServiceApplication.class, args));
        }
    }
}
