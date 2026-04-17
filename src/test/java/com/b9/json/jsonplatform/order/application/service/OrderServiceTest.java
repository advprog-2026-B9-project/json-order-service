package com.b9.json.jsonplatform.order.application.service;

import com.b9.json.jsonplatform.order.domain.Order;
import com.b9.json.jsonplatform.order.infrastructure.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrderWithZeroQuantityShouldThrowException() {
        Order order = new Order();
        order.setQuantity(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(order);
        });

        assertEquals("Jumlah barang harus lebih dari 0", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithValidQuantityShouldSuccess() {
        Order order = new Order();
        order.setQuantity(5);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateStatusFromPaidToCompletedShouldThrowException() {
        Order order = new Order();
        order.setStatus("PAID");
        UUID dummyOrderId = UUID.randomUUID();

        when(orderRepository.findById(dummyOrderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.updateStatus(dummyOrderId, "COMPLETED");
        });

        assertEquals("Pesanan harus dikirim (SHIPPED) sebelum selesai", exception.getMessage());
    }

    @Test
    void testUpdateStatusToShippedShouldSuccess() {
        Order order = new Order();
        order.setStatus("PAID");
        UUID dummyOrderId = UUID.randomUUID();

        when(orderRepository.findById(dummyOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order result = orderService.updateStatus(dummyOrderId, "SHIPPED");

        assertEquals("SHIPPED", result.getStatus());
    }

    @Test
    void testGetTitiperHistory() {
        UUID dummyTitiperId = UUID.randomUUID();

        Order o1 = new Order();
        o1.setTitiperId(dummyTitiperId);
        List<Order> history = Arrays.asList(o1);

        when(orderRepository.findByTitiperId(dummyTitiperId)).thenReturn(history);

        List<Order> result = orderService.getTitiperHistory(dummyTitiperId);

        assertEquals(1, result.size());
        assertEquals(dummyTitiperId, result.get(0).getTitiperId());
    }
}