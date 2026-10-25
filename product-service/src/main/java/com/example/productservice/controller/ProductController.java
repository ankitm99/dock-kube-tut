package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void initData() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("MacBook Pro", "Apple M3 Chip, 16GB RAM", 1999.99));
            productRepository.save(new Product("Mechanical Keyboard", "Wireless RGB Mechanical Keyboard", 129.99));
            productRepository.save(new Product("Dell UltraSharp Monitor", "27-inch 4K UHD Monitor", 449.99));
            productRepository.save(new Product("Sony WH-1000XM5", "Noise Canceling Headphones", 349.99));
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }
}
