package com.b9.json.jsonplatform.order.infrastructure.controller;

import com.b9.json.jsonplatform.order.application.service.OrderService;
import com.b9.json.jsonplatform.order.application.handler.OrderStatusContext;
import com.b9.json.jsonplatform.order.domain.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {OrderController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    // ── GET endpoints ─────────────────────────────────────────────────────────

    @Test
    void testGetHistoryReturnsOk() throws Exception {
        UUID titiperId = UUID.randomUUID();
        Order order = new Order();
        order.setTitiperId(titiperId);

        when(orderService.getTitiperHistory(titiperId)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/history/" + titiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titiperId").value(titiperId.toString()));
    }

    @Test
    void testGetJastiperOrdersReturnsOk() throws Exception {
        UUID jastiperId = UUID.randomUUID();
        Order order = new Order();
        order.setJastiperId(jastiperId);

        when(orderService.getOrdersByJastiper(jastiperId)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/jastiper/" + jastiperId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jastiperId").value(jastiperId.toString()));
    }

    @Test
    void testGetAllOrdersAdminReturnsOk() throws Exception {
        Order order = new Order();
        order.setStatus("PAID");

        when(orderService.getAllOrdersForAdmin()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    void testGetJastiperStatsReturnsOk() throws Exception {
        UUID jastiperId = UUID.randomUUID();
        when(orderService.getTotalSuccessfulOrdersByJastiper(jastiperId)).thenReturn(5L);

        mockMvc.perform(get("/api/orders/jastiper/" + jastiperId + "/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    // ── POST endpoints ────────────────────────────────────────────────────────

    @Test
    void testCheckout_Success() throws Exception {
        Order order = new Order();
        order.setTitiperId(UUID.randomUUID());
        order.setProductId(UUID.randomUUID());
        order.setQuantity(1);
        order.setTotalPrice(new BigDecimal("100000"));
        order.setShippingAddress("Jl. Test No. 1");
        order.setStatus("PAID");

        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void testCheckout_InvalidQuantity_Returns400() throws Exception {
        Order order = new Order();
        order.setQuantity(0);
        order.setTotalPrice(new BigDecimal("100000"));

        when(orderService.createOrder(any()))
                .thenThrow(new IllegalArgumentException("Jumlah barang harus lebih dari 0"));

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGiveRating_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("COMPLETED");
        order.setJastiperRating(5);
        order.setProductRating(4);

        when(orderService.giveRating(eq(orderId), eq(5), eq(4))).thenReturn(order);

        mockMvc.perform(post("/api/orders/" + orderId + "/rate")
                        .param("jastiperRating", "5")
                        .param("productRating", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jastiperRating").value(5));
    }

    @Test
    void testGiveRating_InvalidRating_Returns400() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.giveRating(eq(orderId), eq(6), eq(4)))
                .thenThrow(new IllegalArgumentException("Rating Jastiper harus di antara 1 dan 5"));

        mockMvc.perform(post("/api/orders/" + orderId + "/rate")
                        .param("jastiperRating", "6")
                        .param("productRating", "4"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH endpoints ───────────────────────────────────────────────────────

    @Test
    void testMarkPurchased_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("PURCHASED");

        when(orderService.updateStatus(eq(orderId), eq("PURCHASED"), any(OrderStatusContext.class)))
                .thenReturn(order);

        mockMvc.perform(patch("/api/orders/" + orderId + "/purchased"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PURCHASED"));
    }

    @Test
    void testMarkPurchased_NotPaid_Returns409() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.updateStatus(eq(orderId), eq("PURCHASED"), any(OrderStatusContext.class)))
                .thenThrow(new IllegalStateException("Hanya pesanan berstatus PAID yang bisa diproses"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/purchased"))
                .andExpect(status().isConflict());
    }

    @Test
    void testMarkShipped_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("SHIPPED");
        order.setTrackingNumber("JNE-12345");

        when(orderService.updateStatus(eq(orderId), eq("SHIPPED"), any(OrderStatusContext.class)))
                .thenReturn(order);

        mockMvc.perform(patch("/api/orders/" + orderId + "/shipped")
                        .param("trackingNumber", "JNE-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void testMarkShipped_NoTracking_Returns400() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.updateStatus(eq(orderId), eq("SHIPPED"), any(OrderStatusContext.class)))
                .thenThrow(new IllegalArgumentException("Nomor resi wajib diisi"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/shipped")
                        .param("trackingNumber", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkCompleted_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("COMPLETED");

        when(orderService.updateStatus(eq(orderId), eq("COMPLETED"), any(OrderStatusContext.class)))
                .thenReturn(order);

        mockMvc.perform(patch("/api/orders/" + orderId + "/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testMarkCompleted_NotShipped_Returns409() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.updateStatus(eq(orderId), eq("COMPLETED"), any(OrderStatusContext.class)))
                .thenThrow(new IllegalStateException("Pesanan belum dikirim, tidak bisa diselesaikan"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/completed"))
                .andExpect(status().isConflict());
    }

    @Test
    void testCancelOrder_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus("CANCELLED");

        when(orderService.cancelAndRefundOrder(orderId)).thenReturn(order);

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testCancelOrder_AlreadyShipped_Returns409() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(orderService.cancelAndRefundOrder(orderId))
                .thenThrow(new IllegalStateException("Pesanan tidak dapat dibatalkan pada status ini"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isConflict());
    }
}