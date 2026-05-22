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

        AuthServiceClientImpl.UserInternalDto user = new AuthServiceClientImpl.UserInternalDto();
        user.setId(userId);
        user.setEmail("jastiper@gmail.com");
        user.setUsername("jastiper_budi");

        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/auth/internal/user?id=" + userId,
                AuthServiceClientImpl.UserInternalDto.class
        )).thenReturn(user);

        String result = authServiceClient.findEmailById(userId);
        assertEquals("jastiper@gmail.com", result);
    }

    @Test
    void testFindEmailById_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();

        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/auth/internal/user?id=" + userId,
                AuthServiceClientImpl.UserInternalDto.class
        )).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> authServiceClient.findEmailById(userId));
    }

    @Test
    void testFindUserIdByUsername_Success() {
        UUID expectedId = UUID.randomUUID();

        AuthServiceClientImpl.UserDto user = new AuthServiceClientImpl.UserDto();
        user.setId(expectedId);
        user.setEmail("jastiper@gmail.com");
        user.setUsername("jastiper_budi");

        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/auth/list",
                AuthServiceClientImpl.UserDto[].class
        )).thenReturn(new AuthServiceClientImpl.UserDto[]{user});

        UUID result = authServiceClient.findUserIdByUsername("jastiper_budi");
        assertEquals(expectedId, result);
    }

    @Test
    void testFindUserIdByUsername_NotFound_ThrowsException() {
        AuthServiceClientImpl.UserDto other = new AuthServiceClientImpl.UserDto();
        other.setId(UUID.randomUUID());
        other.setUsername("other_user");

        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/auth/list",
                AuthServiceClientImpl.UserDto[].class
        )).thenReturn(new AuthServiceClientImpl.UserDto[]{other});

        assertThrows(IllegalArgumentException.class,
                () -> authServiceClient.findUserIdByUsername("ghost"));
    }

    @Test
    void testFindUserIdByUsername_NullResponse_ThrowsException() {
        when(restTemplate.getForObject(
                AUTH_URL + "/api/v1/auth/list",
                AuthServiceClientImpl.UserDto[].class
        )).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> authServiceClient.findUserIdByUsername("jastiper_budi"));
    }

    @Test
    void testAddRating_ShouldCallCorrectUrl() {
        authServiceClient.addRating("jastiper@gmail.com", 5);

        verify(restTemplate).postForObject(
                AUTH_URL + "/api/v1/auth/rating?jastiperEmail=jastiper@gmail.com&ratingScore=5",
                null,
                Void.class
        );
    }
}