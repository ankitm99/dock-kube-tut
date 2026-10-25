package com.example.orderservice.controller;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final ProductClient productClient;
    private final OrderRepository orderRepository;

    public OrderController(ProductClient productClient, OrderRepository orderRepository) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void initData() {
        if (orderRepository.count() == 0) {
            orderRepository.save(new Order(
                    1L,
                    "MacBook Pro",
                    1,
                    1999.99,
                    1999.99,
                    "CONFIRMED",
                    Instant.now()
            ));
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        if (request.quantity() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be greater than 0"));
        }

        // Inter-service call to product-service via DNS discovery
        Optional<ProductDTO> productOpt = productClient.getProductById(request.productId());
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Product not found with id: " + request.productId()));
        }

        ProductDTO product = productOpt.get();
        double totalPrice = product.price() * request.quantity();

        Order order = new Order(
                product.id(),
                product.name(),
                request.quantity(),
                product.price(),
                totalPrice,
                "CONFIRMED",
                Instant.now()
        );

        Order saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
