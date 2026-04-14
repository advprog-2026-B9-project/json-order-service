package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceDummy;
import com.b9.json.jsonplatform.wallet.application.WalletService;
import com.b9.json.jsonplatform.wallet.application.TransactionService;
import com.b9.json.jsonplatform.wallet.domain.Transaction;
import com.b9.json.jsonplatform.wallet.domain.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final InventoryServiceDummy inventoryService;

    public OrderService(OrderRepository orderRepository,
                        WalletService walletService,
                        TransactionService transactionService,
                        InventoryServiceDummy inventoryService) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.inventoryService = inventoryService;
    }

    public Order createOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Jumlah barang harus lebih dari 0");
        }
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total harga tidak valid");
        }

        if (!inventoryService.isStockAvailable(order.getProductId(), order.getQuantity())) {
            throw new IllegalStateException("Stok barang tidak mencukupi");
        }
        
        // Ambil data dompet dari Modul Wallet
        Wallet buyerWallet = walletService.getWalletByUserId(order.getTitiperId());
        Wallet sellerWallet = walletService.getWalletByUserId(order.getJastiperId());

        if (buyerWallet.getBalance().compareTo(order.getTotalPrice()) < 0) {
            throw new IllegalStateException("Saldo Wallet tidak mencukupi");
        }

        // Buat transaksi PAYMENT via Modul Wallet
        Transaction payment = transactionService.createPayment(
                buyerWallet.getId(),
                sellerWallet.getId(),
                order.getTotalPrice()
        );

        transactionService.markSuccess(payment.getId());
        inventoryService.reserveStock(order.getProductId(), order.getQuantity());
        order.setStatus("PAID");

        return orderRepository.save(order);
    }

    public Order updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        if (order.getStatus().equals("PAID") && newStatus.equals("COMPLETED")) {
            throw new IllegalStateException("Pesanan harus dikirim (SHIPPED) sebelum selesai");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getTitiperHistory(UUID titiperId) {
        return orderRepository.findByTitiperId(titiperId);
    }
}