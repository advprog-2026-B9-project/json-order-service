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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WalletServiceClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WalletServiceClientImpl walletServiceClient;

    private static final String WALLET_URL = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(walletServiceClient, "walletServiceUrl", WALLET_URL);
    }

    @Test
    void testGetWalletId_Success() {
        UUID userId = UUID.randomUUID();
        UUID expectedWalletId = UUID.randomUUID();

        WalletServiceClientImpl.WalletDto wallet = new WalletServiceClientImpl.WalletDto();
        wallet.setId(expectedWalletId);
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("500000"));

        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/users/" + userId,
                WalletServiceClientImpl.WalletDto.class
        )).thenReturn(wallet);

        UUID result = walletServiceClient.getWalletId(userId);
        assertEquals(expectedWalletId, result);
    }

    @Test
    void testGetWalletId_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();

        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/users/" + userId,
                WalletServiceClientImpl.WalletDto.class
        )).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> walletServiceClient.getWalletId(userId));
    }

    @Test
    void testGetBalance_Success() {
        UUID userId = UUID.randomUUID();

        WalletServiceClientImpl.WalletDto wallet = new WalletServiceClientImpl.WalletDto();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal("500000"));

        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/users/" + userId,
                WalletServiceClientImpl.WalletDto.class
        )).thenReturn(wallet);

        BigDecimal result = walletServiceClient.getBalance(userId);
        assertEquals(new BigDecimal("500000"), result);
    }

    @Test
    void testGetBalance_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();

        when(restTemplate.getForObject(
                WALLET_URL + "/api/v1/wallets/users/" + userId,
                WalletServiceClientImpl.WalletDto.class
        )).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> walletServiceClient.getBalance(userId));
    }

    @Test
    void testCreatePayment_TxNotNull_ShouldMarkSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");
        UUID txId = UUID.randomUUID();

        WalletServiceClientImpl.TransactionDto tx = new WalletServiceClientImpl.TransactionDto();
        tx.setId(txId);
        tx.setStatus("PENDING");

        when(restTemplate.postForObject(
                contains("/api/v1/transactions/payment"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        )).thenReturn(tx);

        walletServiceClient.createPayment(buyerWalletId, sellerWalletId, amount);

        verify(restTemplate).postForObject(
                contains("/api/v1/transactions/" + txId + "/success"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        );
    }

    @Test
    void testCreatePayment_TxNull_ShouldNotCallSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100000");

        when(restTemplate.postForObject(
                contains("/api/v1/transactions/payment"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        )).thenReturn(null);

        walletServiceClient.createPayment(buyerWalletId, sellerWalletId, amount);

        // Verify /success tidak dipanggil
        verify(restTemplate, never()).postForObject(
                contains("/success"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        );
    }

    @Test
    void testCreateRefund_TxNotNull_ShouldMarkSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200000");
        UUID txId = UUID.randomUUID();

        WalletServiceClientImpl.TransactionDto tx = new WalletServiceClientImpl.TransactionDto();
        tx.setId(txId);
        tx.setStatus("PENDING");

        when(restTemplate.postForObject(
                contains("/api/v1/transactions/refund"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        )).thenReturn(tx);

        walletServiceClient.createRefund(buyerWalletId, sellerWalletId, amount);

        verify(restTemplate).postForObject(
                contains("/api/v1/transactions/" + txId + "/success"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        );
    }

    @Test
    void testCreateRefund_TxNull_ShouldNotCallSuccess() {
        UUID buyerWalletId = UUID.randomUUID();
        UUID sellerWalletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("200000");

        when(restTemplate.postForObject(
                contains("/api/v1/transactions/refund"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        )).thenReturn(null);

        walletServiceClient.createRefund(buyerWalletId, sellerWalletId, amount);

        verify(restTemplate, never()).postForObject(
                contains("/success"),
                isNull(),
                eq(WalletServiceClientImpl.TransactionDto.class)
        );
    }
}