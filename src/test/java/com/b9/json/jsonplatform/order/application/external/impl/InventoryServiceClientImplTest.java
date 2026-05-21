package com.b9.json.jsonplatform.order.application.external.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryServiceClientImpl inventoryServiceClient;

    private static final String INVENTORY_URL = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(inventoryServiceClient, "inventoryServiceUrl", INVENTORY_URL);
    }

    @Test
    void testGetProductName_Success() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/name",
                String.class
        )).thenReturn("Sepatu Nike");

        String result = inventoryServiceClient.getProductName(productId);

        assertEquals("Sepatu Nike", result);
    }

    @Test
    void testGetProductOwnerUsername_Success() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/owner",
                String.class
        )).thenReturn("jastiper_budi");

        String result = inventoryServiceClient.getProductOwnerUsername(productId);

        assertEquals("jastiper_budi", result);
    }

    @Test
    void testDeductStock_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.deductStock(productId, 3);

        verify(restTemplate).patchForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/stock/deduct?quantity=3",
                null,
                Void.class
        );
    }

    @Test
    void testIncreaseStock_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.increaseStock(productId, 2);

        verify(restTemplate).patchForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/stock/increase?quantity=2",
                null,
                Void.class
        );
    }

    @Test
    void testAddProductRating_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();

        inventoryServiceClient.addProductRating(productId, 4);

        verify(restTemplate).postForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/rating?score=4",
                null,
                Void.class
        );
    }
}