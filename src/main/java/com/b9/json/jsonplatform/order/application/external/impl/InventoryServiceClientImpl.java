package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryServiceClientImpl implements InventoryServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.inventory.url}")
    private String inventoryServiceUrl;

    // GET /api/v1/products/{id} → return full Product object
    private ProductDto getProduct(UUID productId) {
        return restTemplate.getForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId,
                ProductDto.class
        );
    }

    @Override
    public String getProductName(UUID productId) {
        ProductDto product = getProduct(productId);
        if (product == null) throw new IllegalArgumentException("Produk tidak ditemukan: " + productId);
        return product.getName();
    }

    @Override
    public String getProductOwnerUsername(UUID productId) {
        ProductDto product = getProduct(productId);
        if (product == null) throw new IllegalArgumentException("Produk tidak ditemukan: " + productId);
        return product.getOwnerUsername();
    }

    @Override
    public void deductStock(UUID productId, int quantity) {
        // PUT /api/v1/products/{id}/deduct-stock?quantity=
        restTemplate.put(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/deduct-stock?quantity=" + quantity,
                null
        );
    }

    @Override
    public void increaseStock(UUID productId, int quantity) {
        // PUT /api/v1/products/{id}/increase-stock?quantity=
        restTemplate.put(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/increase-stock?quantity=" + quantity,
                null
        );
    }

    @Override
    public void addProductRating(UUID productId, int rating) {
        // POST /api/v1/products/{id}/rating?ratingScore=
        restTemplate.postForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/rating?ratingScore=" + rating,
                null,
                Void.class
        );
    }

    @Getter @Setter
    static class ProductDto {
        private UUID id;
        private String name;
        private String ownerUsername;
        private java.math.BigDecimal price;
        private int stock;
    }
}