package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import lombok.RequiredArgsConstructor;
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

    @Override
    public String getProductName(UUID productId) {
        return restTemplate.getForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/name",
                String.class
        );
    }

    @Override
    public String getProductOwnerUsername(UUID productId) {
        return restTemplate.getForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/owner",
                String.class
        );
    }

    @Override
    public void deductStock(UUID productId, int quantity) {
        restTemplate.patchForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/stock/deduct?quantity=" + quantity,
                null,
                Void.class
        );
    }

    @Override
    public void increaseStock(UUID productId, int quantity) {
        restTemplate.patchForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/stock/increase?quantity=" + quantity,
                null,
                Void.class
        );
    }

    @Override
    public void addProductRating(UUID productId, int rating) {
        restTemplate.postForObject(
                inventoryServiceUrl + "/api/v1/products/" + productId + "/rating?score=" + rating,
                null,
                Void.class
        );
    }
}