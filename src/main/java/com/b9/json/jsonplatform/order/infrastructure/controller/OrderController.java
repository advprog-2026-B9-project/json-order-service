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
    public ResponseEntity<?> markPurchased(@PathVariable UUID orderId) {
        try {
            OrderStatusContext ctx = OrderStatusContext.builder().build();
            return ResponseEntity.ok(orderService.updateStatus(orderId, "PURCHASED", ctx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{orderId}/shipped")
    public ResponseEntity<?> markShipped(@PathVariable UUID orderId,
                                         @RequestParam String trackingNumber) {
        try {
            OrderStatusContext ctx = OrderStatusContext.builder()
                    .trackingNumber(trackingNumber)
                    .build();
            return ResponseEntity.ok(orderService.updateStatus(orderId, "SHIPPED", ctx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{orderId}/completed")
    public ResponseEntity<?> markCompleted(@PathVariable UUID orderId) {
        try {
            OrderStatusContext ctx = OrderStatusContext.builder().build();
            return ResponseEntity.ok(orderService.updateStatus(orderId, "COMPLETED", ctx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId) {
        try {
            return ResponseEntity.ok(orderService.cancelAndRefundOrder(orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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