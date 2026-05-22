package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletServiceClientImpl implements WalletServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.wallet.url}")
    private String walletServiceUrl;

    // GET /api/v1/wallets/users/{id}
    private WalletDto getWallet(UUID userId) {
        return restTemplate.getForObject(
                walletServiceUrl + "/api/v1/wallets/users/" + userId,
                WalletDto.class
        );
    }

    @Override
    public UUID getWalletId(UUID userId) {
        WalletDto wallet = getWallet(userId);
        if (wallet == null) throw new IllegalStateException("Wallet tidak ditemukan untuk user: " + userId);
        return wallet.getId();
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        WalletDto wallet = getWallet(userId);
        if (wallet == null) throw new IllegalStateException("Wallet tidak ditemukan untuk user: " + userId);
        return wallet.getBalance();
    }

    @Override
    public void createPayment(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        // POST /api/v1/transactions/payment?walletId=&targetWalletId=&amount=
        String url = UriComponentsBuilder
                .fromHttpUrl(walletServiceUrl + "/api/v1/transactions/payment")
                .queryParam("walletId", buyerWalletId)
                .queryParam("targetWalletId", sellerWalletId)
                .queryParam("amount", amount)
                .toUriString();

        TransactionDto tx = restTemplate.postForObject(url, null, TransactionDto.class);

        if (tx != null) {
            restTemplate.postForObject(
                    walletServiceUrl + "/api/v1/transactions/" + tx.getId() + "/success",
                    null, TransactionDto.class
            );
        }
    }

    @Override
    public void createRefund(UUID buyerWalletId, UUID sellerWalletId, BigDecimal amount) {
        // POST /api/v1/transactions/refund?walletId=sellerWalletId&targetWalletId=buyerWalletId&amount=
        String url = UriComponentsBuilder
                .fromHttpUrl(walletServiceUrl + "/api/v1/transactions/refund")
                .queryParam("walletId", sellerWalletId)
                .queryParam("targetWalletId", buyerWalletId)
                .queryParam("amount", amount)
                .toUriString();

        TransactionDto tx = restTemplate.postForObject(url, null, TransactionDto.class);

        if (tx != null) {
            restTemplate.postForObject(
                    walletServiceUrl + "/api/v1/transactions/" + tx.getId() + "/success",
                    null, TransactionDto.class
            );
        }
    }

    @Getter @Setter
    static class WalletDto {
        private UUID id;
        private UUID userId;
        private BigDecimal balance;
    }

    @Getter @Setter
    static class TransactionDto {
        private UUID id;
        private String status;
        private String type;
        private BigDecimal amount;
    }
}