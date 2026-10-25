package com.example.orderservice.client;

import com.example.orderservice.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url}") String productServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    public Optional<ProductDTO> getProductById(Long productId) {
        try {
            ProductDTO product = restClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductDTO.class);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
