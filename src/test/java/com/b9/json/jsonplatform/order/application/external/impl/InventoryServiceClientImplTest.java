package com.b9.json.jsonplatform.order.application.external.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryServiceClientImpl inventoryServiceClient;

    private static final String INVENTORY_URL = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(inventoryServiceClient, "inventoryServiceUrl", INVENTORY_URL);
    }

    private InventoryServiceClientImpl.ProductDto makeProduct(UUID id, String name, String owner) {
        InventoryServiceClientImpl.ProductDto p = new InventoryServiceClientImpl.ProductDto();
        p.setId(id);
        p.setName(name);
        p.setOwnerUsername(owner);
        p.setPrice(new BigDecimal("100000"));
        p.setStock(10);
        return p;
    }

    @Test
    void testGetProductName_Success() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId,
                InventoryServiceClientImpl.ProductDto.class
        )).thenReturn(makeProduct(productId, "Sepatu Nike", "jastiper_budi"));

        assertEquals("Sepatu Nike", inventoryServiceClient.getProductName(productId));
    }

    @Test
    void testGetProductName_NotFound_ThrowsException() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId,
                InventoryServiceClientImpl.ProductDto.class
        )).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> inventoryServiceClient.getProductName(productId));
    }

    @Test
    void testGetProductOwnerUsername_Success() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId,
                InventoryServiceClientImpl.ProductDto.class
        )).thenReturn(makeProduct(productId, "Sepatu Nike", "jastiper_budi"));

        assertEquals("jastiper_budi", inventoryServiceClient.getProductOwnerUsername(productId));
    }

    @Test
    void testGetProductOwnerUsername_NotFound_ThrowsException() {
        UUID productId = UUID.randomUUID();
        when(restTemplate.getForObject(
                INVENTORY_URL + "/api/v1/products/" + productId,
                InventoryServiceClientImpl.ProductDto.class
        )).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> inventoryServiceClient.getProductOwnerUsername(productId));
    }

    @Test
    void testDeductStock_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();
        inventoryServiceClient.deductStock(productId, 3);

        verify(restTemplate).put(
                INVENTORY_URL + "/api/v1/products/" + productId + "/deduct-stock?quantity=3",
                null
        );
    }

    @Test
    void testIncreaseStock_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();
        inventoryServiceClient.increaseStock(productId, 2);

        verify(restTemplate).put(
                INVENTORY_URL + "/api/v1/products/" + productId + "/increase-stock?quantity=2",
                null
        );
    }

    @Test
    void testAddProductRating_ShouldCallCorrectUrl() {
        UUID productId = UUID.randomUUID();
        inventoryServiceClient.addProductRating(productId, 4);

        verify(restTemplate).postForObject(
                INVENTORY_URL + "/api/v1/products/" + productId + "/rating?ratingScore=4",
                null,
                Void.class
        );
    }
}