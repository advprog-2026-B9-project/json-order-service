package com.b9.json.jsonplatform.order.application.external.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceClientImpl authServiceClient;

    private static final String AUTH_URL = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authServiceClient, "authServiceUrl", AUTH_URL);
    }

    @Test
    void testFindEmailById_Success() {
        UUID userId = UUID.randomUUID();
        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/users/" + userId + "/email",
                String.class
        )).thenReturn("jastiper@gmail.com");

        String result = authServiceClient.findEmailById(userId);

        assertEquals("jastiper@gmail.com", result);
    }

    @Test
    void testFindEmailById_ReturnsNull_WhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/users/" + userId + "/email",
                String.class
        )).thenReturn(null);

        String result = authServiceClient.findEmailById(userId);

        assertNull(result);
    }

    @Test
    void testFindUserIdByUsername_Success() {
        UUID expectedId = UUID.randomUUID();
        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/users/username/jastiper_budi/id",
                UUID.class
        )).thenReturn(expectedId);

        UUID result = authServiceClient.findUserIdByUsername("jastiper_budi");

        assertEquals(expectedId, result);
    }

    @Test
    void testFindUserIdByUsername_ReturnsNull_WhenNotFound() {
        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/users/username/ghost/id",
                UUID.class
        )).thenReturn(null);

        UUID result = authServiceClient.findUserIdByUsername("ghost");

        assertNull(result);
    }

    @Test
    void testAddRating_ShouldCallCorrectUrl() {
        authServiceClient.addRating("jastiper@gmail.com", 5);

        verify(restTemplate).postForObject(
                AUTH_URL + "/api/v1/users/jastiper@gmail.com/rating?score=5",
                null,
                Void.class
        );
    }
}