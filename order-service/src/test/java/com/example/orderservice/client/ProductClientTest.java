package com.example.orderservice.client;

import com.example.orderservice.dto.ProductDTO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductClientTest {

    private HttpServer server;
    private ProductClient productClient;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/products/", exchange -> {
            String productId = exchange.getRequestURI().getPath().substring("/products/".length());
            if ("3".equals(productId)) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            byte[] response = ("2".equals(productId) ? "" :
                    """
                    {"id":1,"name":"Monitor","description":"4K display","price":299.5}
                    """).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        productClient = new ProductClient("http://localhost:" + server.getAddress().getPort());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void returnsProductForSuccessfulResponse() {
        Optional<ProductDTO> result = productClient.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals(new ProductDTO(1L, "Monitor", "4K display", 299.5), result.get());
    }

    @Test
    void returnsEmptyWhenResponseHasNoProductBody() {
        assertTrue(productClient.getProductById(2L).isEmpty());
    }

    @Test
    void returnsEmptyWhenProductServiceReturnsAnError() {
        assertTrue(productClient.getProductById(3L).isEmpty());
    }
}
