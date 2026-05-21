package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletServiceClientImpl implements WalletServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.wallet.url}")
    private String walletServiceUrl;

    @Override
    public UUID getWalletId(UUID userId) {
        return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/user/" + userId + "/id",
                UUID.class
        );
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/user/" + userId + "/balance",
                BigDecimal.class
        );
    }

    @Override
    public void createPayment(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        restTemplate.postForObject(
                walletServiceUrl + "/api/v1/wallets/payment",
                Map.of(
                        "buyerWalletId", buyerWalletId,
                        "sellerWalletId", sellerWalletId,
                        "amount", amount
                ),
                Void.class
        );
    }

    @Override
    public void createRefund(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        restTemplate.postForObject(
                walletServiceUrl + "/api/v1/wallets/refund",
                Map.of(
                        "sellerWalletId", sellerWalletId,
                        "buyerWalletId", buyerWalletId,
                        "amount", amount
                ),
                Void.class
        );
    }
}