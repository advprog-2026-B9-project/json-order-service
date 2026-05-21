package com.b9.json.jsonplatform.order.application.external.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WalletServiceClientImpl walletServiceClient;

    private static final String WALLET_URL = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(walletServiceClient, "walletServiceUrl", WALLET_URL);
    }

    @Test
    void testGetWalletId_Success() {
        UUID userId = UUID.randomUUID();
        UUID expectedWalletId = UUID.randomUUID();
        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/user/" + userId + "/id",
                UUID.class
        )).thenReturn(expectedWalletId);

        UUID result = walletServiceClient.getWalletId(userId);

        assertEquals(expectedWalletId, result);
    }

    @Test
    void testGetBalance_Success() {
        UUID userId = UUID.randomUUID();
        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/user/" + userId + "/balance",
                BigDecimal.class
        )).thenReturn(new BigDecimal("500000"));

        BigDecimal result = walletServiceClient.getBalance(userId);

        assertEquals(new BigDecimal("500000"), result);
    }

    @Test
    void testCreatePayment_ShouldCallCorrectUrlWithBody() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");

        walletServiceClient.createPayment(buyerWalletId, sellerWalletId, amount);

        verify(restTemplate).postForObject(
                eq(WALLET_URL + "/api/v1/wallets/payment"),
                eq(Map.of(
                        "buyerWalletId", buyerWalletId,
                        "sellerWalletId", sellerWalletId,
                        "amount", amount
                )),
                eq(Void.class)
        );
    }

    @Test
    void testCreateRefund_ShouldCallCorrectUrlWithBody() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200000");

        walletServiceClient.createRefund(buyerWalletId, sellerWalletId, amount);

        verify(restTemplate).postForObject(
                eq(WALLET_URL + "/api/v1/wallets/refund"),
                eq(Map.of(
                        "sellerWalletId", sellerWalletId,
                        "buyerWalletId", buyerWalletId,
                        "amount", amount
                )),
                eq(Void.class)
        );
    }
}