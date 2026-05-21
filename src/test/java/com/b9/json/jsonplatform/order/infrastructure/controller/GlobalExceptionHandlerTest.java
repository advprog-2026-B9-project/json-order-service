package com.b9.json.jsonplatform.order.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequest_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("input tidak valid");

        ResponseEntity<String> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("input tidak valid", response.getBody());
    }

    @Test
    void handleConflict_ShouldReturn409() {
        IllegalStateException ex = new IllegalStateException("status tidak valid");

        ResponseEntity<String> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("status tidak valid", response.getBody());
    }

    @Test
    void handleGeneral_ShouldReturn500() {
        Exception ex = new RuntimeException("unexpected error");

        ResponseEntity<String> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Terjadi kesalahan pada server", response.getBody());
    }
}