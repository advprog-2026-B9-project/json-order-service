package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import com.b9.json.jsonplatform.order.application.external.InventoryServiceClient;
import com.b9.json.jsonplatform.order.application.external.WalletServiceClient;
import com.b9.json.jsonplatform.order.application.handler.*;
import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private WalletServiceClient walletServiceClient;
    @Mock private InventoryServiceClient inventoryServiceClient;
    @Mock private AuthServiceClient authServiceClient;

    private OrderService orderService;

    private final UUID titiperId   = UUID.randomUUID();
    private final UUID jastiperId  = UUID.randomUUID();
    private final UUID productId   = UUID.randomUUID();
    private final UUID orderId     = UUID.randomUUID();
    private final UUID buyerWallet = UUID.randomUUID();
    private final UUID sellerWallet= UUID.randomUUID();

    @BeforeEach
    void setUp() {
        List<OrderStatusHandler> handlers = List.of(
                new PurchasedHandler(),
                new ShippedHandler(),
                new CompletedHandler()
        );
        Map<String, OrderStatusHandler> handlerMap = new HashMap<>();
        handlers.forEach(h -> handlerMap.put(h.supportedStatus(), h));

        orderService = new OrderService(
                orderRepository,
                walletServiceClient,
                inventoryServiceClient,
                authServiceClient,
                handlerMap
        );
    }

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    void createOrder_Success() {
        Order order = buildOrder(null);
        order.setTitiperId(titiperId);
        order.setProductId(productId);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("100000"));
        order.setShippingAddress("Jl. Test No. 1");

        when(inventoryServiceClient.getProductName(productId)).thenReturn("Produk Test");
        when(walletServiceClient.getBalance(titiperId)).thenReturn(new BigDecimal("500000"));
        when(walletServiceClient.getWalletId(titiperId)).thenReturn(buyerWallet);
        when(walletServiceClient.getWalletId(jastiperId)).thenReturn(sellerWallet);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.createOrder(order);

        assertEquals("PAID", result.getStatus());
        assertEquals("Produk Test", result.getProductName());
        assertEquals(jastiperId, result.getJastiperId());
        verify(walletServiceClient).createPayment(buyerWallet, sellerWallet, new BigDecimal("100000"));
        verify(inventoryServiceClient).deductStock(productId, 2);
        verify(orderRepository).save(any());
    }

    @Test
    void createOrder_QuantityZero_ThrowsException() {
        Order order = buildOrder(null);
        order.setQuantity(0);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_NegativeQuantity_ThrowsException() {
        Order order = buildOrder(null);
        order.setQuantity(-1);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));
    }

    @Test
    void createOrder_InvalidShippingAddress_ThrowsException() {
        Order order = buildOrder(null);
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("100000"));
        order.setShippingAddress("Jl. <script>alert(1)</script>");

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));
    }

    @Test
    void createOrder_InsufficientBalance_ThrowsException() {
        Order order = buildOrder(null);
        order.setTitiperId(titiperId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("999999999"));
        order.setShippingAddress("Jl. Test");

        when(inventoryServiceClient.getProductName(productId)).thenReturn("Produk");
        when(walletServiceClient.getBalance(titiperId)).thenReturn(new BigDecimal("100000"));

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(order));
        verify(walletServiceClient, never()).createPayment(any(), any(), any());
    }

    @Test
    void createOrder_NullShippingAddress_ShouldNotThrow() {
        Order order = buildOrder(null);
        order.setQuantity(1);
        order.setShippingAddress(null);
        order.setTotalPrice(new BigDecimal("100000"));

        when(inventoryServiceClient.getProductName(productId)).thenReturn("Produk");
        when(walletServiceClient.getBalance(titiperId)).thenReturn(new BigDecimal("500000"));
        when(walletServiceClient.getWalletId(titiperId)).thenReturn(buyerWallet);
        when(walletServiceClient.getWalletId(jastiperId)).thenReturn(sellerWallet);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.createOrder(order);

        assertEquals("PAID", result.getStatus());
    }

    @Test
    void createOrder_NullQuantity_ThrowsException() {
        Order order = buildOrder(null);
        order.setQuantity(null);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    void updateStatus_ToPurchased_Success() {
        Order order = buildOrder("PAID");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateStatus(orderId, "PURCHASED", OrderStatusContext.builder().build());

        assertEquals("PURCHASED", result.getStatus());
    }

    @Test
    void updateStatus_ToPurchased_NotPaid_ThrowsException() {
        Order order = buildOrder("PENDING");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "PURCHASED", OrderStatusContext.builder().build()));
    }

    @Test
    void updateStatus_ToShipped_Success() {
        Order order = buildOrder("PURCHASED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateStatus(orderId, "SHIPPED",
                OrderStatusContext.builder().trackingNumber("JNE-12345").build());

        assertEquals("SHIPPED", result.getStatus());
        assertEquals("JNE-12345", result.getTrackingNumber());
    }

    @Test
    void updateStatus_ToShipped_NoTrackingNumber_ThrowsException() {
        Order order = buildOrder("PURCHASED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateStatus(orderId, "SHIPPED",
                        OrderStatusContext.builder().trackingNumber("").build()));
    }

    @Test
    void updateStatus_ToShipped_NotPurchased_ThrowsException() {
        Order order = buildOrder("PAID");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "SHIPPED",
                        OrderStatusContext.builder().trackingNumber("JNE-12345").build()));
    }

    @Test
    void updateStatus_ToCompleted_Success() {
        Order order = buildOrder("SHIPPED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateStatus(orderId, "COMPLETED", OrderStatusContext.builder().build());

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void updateStatus_ToCompleted_NotShipped_ThrowsException() {
        Order order = buildOrder("PAID");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () ->
                orderService.updateStatus(orderId, "COMPLETED", OrderStatusContext.builder().build()));
    }

    @Test
    void updateStatus_InvalidStatus_ThrowsException() {
        Order order = buildOrder("PAID");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateStatus(orderId, "INVALID", OrderStatusContext.builder().build()));
    }

    @Test
    void updateStatus_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateStatus(orderId, "PURCHASED", OrderStatusContext.builder().build()));
    }

    // ── cancelAndRefundOrder ──────────────────────────────────────────────────

    @Test
    void cancelAndRefundOrder_Success() {
        Order order = buildOrder("PAID");
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("100000"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(walletServiceClient.getWalletId(titiperId)).thenReturn(buyerWallet);
        when(walletServiceClient.getWalletId(jastiperId)).thenReturn(sellerWallet);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.cancelAndRefundOrder(orderId);

        assertEquals("CANCELLED", result.getStatus());
        verify(walletServiceClient).createRefund(buyerWallet, sellerWallet, new BigDecimal("100000"));
        verify(inventoryServiceClient).increaseStock(productId, 2);
    }

    @Test
    void cancelAndRefundOrder_AlreadyShipped_ThrowsException() {
        Order order = buildOrder("SHIPPED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelAndRefundOrder(orderId));
        verify(walletServiceClient, never()).createRefund(any(), any(), any());
    }

    @Test
    void cancelAndRefundOrder_AlreadyCompleted_ThrowsException() {
        Order order = buildOrder("COMPLETED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelAndRefundOrder(orderId));
    }

    @Test
    void cancelAndRefundOrder_AlreadyCancelled_ThrowsException() {
        Order order = buildOrder("CANCELLED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelAndRefundOrder(orderId));
    }

    // ── giveRating ────────────────────────────────────────────────────────────

    @Test
    void giveRating_Success() {
        Order order = buildOrder("COMPLETED");
        order.setJastiperId(jastiperId);
        order.setProductId(productId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authServiceClient.findEmailById(jastiperId)).thenReturn("jastiper@example.com");
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.giveRating(orderId, 5, 4);

        assertEquals(5, result.getJastiperRating());
        assertEquals(4, result.getProductRating());
        verify(inventoryServiceClient).addProductRating(productId, 4);
        verify(authServiceClient).addRating("jastiper@example.com", 5);
    }

    @Test
    void giveRating_NotCompleted_ThrowsException() {
        Order order = buildOrder("SHIPPED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.giveRating(orderId, 5, 4));
    }

    @Test
    void giveRating_JastiperRatingTooHigh_ThrowsException() {
        Order order = buildOrder("COMPLETED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.giveRating(orderId, 6, 4));
    }

    @Test
    void giveRating_JastiperRatingTooLow_ThrowsException() {
        Order order = buildOrder("COMPLETED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.giveRating(orderId, 0, 4));
    }

    @Test
    void giveRating_ProductRatingNull_ThrowsException() {
        Order order = buildOrder("COMPLETED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.giveRating(orderId, 5, null));
    }

    // ── query methods ─────────────────────────────────────────────────────────

    @Test
    void getTitiperHistory_ReturnsList() {
        List<Order> orders = List.of(buildOrder("PAID"), buildOrder("COMPLETED"));
        when(orderRepository.findByTitiperId(titiperId)).thenReturn(orders);

        List<Order> result = orderService.getTitiperHistory(titiperId);

        assertEquals(2, result.size());
    }

    @Test
    void getOrdersByJastiper_ReturnsList() {
        List<Order> orders = List.of(buildOrder("SHIPPED"));
        when(orderRepository.findByJastiperId(jastiperId)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByJastiper(jastiperId);

        assertEquals(1, result.size());
    }

    @Test
    void getTotalSuccessfulOrdersByJastiper_ReturnsCount() {
        when(orderRepository.countByJastiperIdAndStatus(jastiperId, "COMPLETED")).thenReturn(5L);

        long result = orderService.getTotalSuccessfulOrdersByJastiper(jastiperId);

        assertEquals(5L, result);
    }

    @Test
    void getAllOrdersForAdmin_ReturnsList() {
        List<Order> orders = List.of(buildOrder("PAID"), buildOrder("SHIPPED"), buildOrder("COMPLETED"));
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrdersForAdmin();

        assertEquals(3, result.size());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Order buildOrder(String status) {
        Order order = new Order();
        order.setId(orderId);
        order.setTitiperId(titiperId);
        order.setJastiperId(jastiperId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("100000"));
        order.setShippingAddress("Jl. Test No. 1");
        if (status != null) order.setStatus(status);
        return order;
    }
}