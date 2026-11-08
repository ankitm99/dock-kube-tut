package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController controller;

    @Test
    void initDataSeedsProductsWhenRepositoryIsEmpty() {
        when(productRepository.count()).thenReturn(0L);

        controller.initData();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(4)).save(productCaptor.capture());
        List<Product> products = productCaptor.getAllValues();
        assertEquals("MacBook Pro", products.get(0).getName());
        assertEquals("Apple M3 Chip, 16GB RAM", products.get(0).getDescription());
        assertEquals(1999.99, products.get(0).getPrice());
        assertEquals("Mechanical Keyboard", products.get(1).getName());
        assertEquals("Dell UltraSharp Monitor", products.get(2).getName());
        assertEquals("Sony WH-1000XM5", products.get(3).getName());
    }

    @Test
    void initDataDoesNotSeedProductsWhenRepositoryIsNotEmpty() {
        when(productRepository.count()).thenReturn(1L);

        controller.initData();

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProductsReturnsRepositoryResults() {
        List<Product> products = List.of(new Product(2L, "Keyboard", "Wireless", 25.0));
        when(productRepository.findAll()).thenReturn(products);

        assertSame(products, controller.getAllProducts());
    }

    @Test
    void getProductByIdReturnsProductWhenFound() {
        Product product = new Product(7L, "Mouse", "Wireless", 10.0);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        ResponseEntity<Product> response = controller.getProductById(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(product, response.getBody());
    }

    @Test
    void getProductByIdReturnsNotFoundWhenMissing() {
        when(productRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = controller.getProductById(7L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createProductSavesAndReturnsProduct() {
        Product product = new Product("Headphones", "Wireless", 49.99);
        when(productRepository.save(product)).thenReturn(product);

        assertSame(product, controller.createProduct(product));

        verify(productRepository).save(product);
    }
}
