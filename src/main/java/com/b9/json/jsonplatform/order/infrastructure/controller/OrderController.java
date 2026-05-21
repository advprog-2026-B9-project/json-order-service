package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.application.handler.OrderStatusContext;
import com.b9.json.jsonplatform.order.application.service.OrderService;
import com.b9.json.jsonplatform.order.domain.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Order order) {
        try {
            return ResponseEntity.ok(orderService.createOrder(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{titiperId}")
    public ResponseEntity<List<Order>> getTitiperHistory(@PathVariable UUID titiperId) {
        return ResponseEntity.ok(orderService.getTitiperHistory(titiperId));
    }

    @GetMapping("/jastiper/{jastiperId}")
    public ResponseEntity<List<Order>> getJastiperOrders(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.getOrdersByJastiper(jastiperId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    @GetMapping("/jastiper/{jastiperId}/stats")
    public ResponseEntity<Long> getJastiperStats(@PathVariable UUID jastiperId) {
        return ResponseEntity.ok(orderService.getTotalSuccessfulOrdersByJastiper(jastiperId));
    }

    @PatchMapping("/{orderId}/purchased")
    public ResponseEntity<Order> markPurchased(@PathVariable UUID orderId) {
        OrderStatusContext ctx = OrderStatusContext.builder().build();
        return ResponseEntity.ok(orderService.updateStatus(orderId, "PURCHASED", ctx));
    }

    @PatchMapping("/{orderId}/shipped")
    public ResponseEntity<Order> markShipped(@PathVariable UUID orderId,
                                             @RequestParam String trackingNumber) {
        OrderStatusContext ctx = OrderStatusContext.builder()
                .trackingNumber(trackingNumber)
                .build();
        return ResponseEntity.ok(orderService.updateStatus(orderId, "SHIPPED", ctx));
    }

    @PatchMapping("/{orderId}/completed")
    public ResponseEntity<Order> markCompleted(@PathVariable UUID orderId) {
        OrderStatusContext ctx = OrderStatusContext.builder().build();
        return ResponseEntity.ok(orderService.updateStatus(orderId, "COMPLETED", ctx));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelAndRefundOrder(orderId));
    }

    @PostMapping("/{orderId}/rate")
    public ResponseEntity<?> giveRating(@PathVariable UUID orderId,
                                        @RequestParam Integer jastiperRating,
                                        @RequestParam Integer productRating) {
        try {
            return ResponseEntity.ok(orderService.giveRating(orderId, jastiperRating, productRating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}