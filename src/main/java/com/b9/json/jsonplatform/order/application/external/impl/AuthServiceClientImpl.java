package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthServiceClientImpl implements AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.auth.url}")
    private String authServiceUrl;

    @Override
    public String findEmailById(UUID userId) {
        return restTemplate.getForObject(
                authServiceUrl + "/api/v1/users/" + userId + "/email",
                String.class
        );
    }

    @Override
    public void addRating(String email, int rating) {
        restTemplate.postForObject(
                authServiceUrl + "/api/v1/users/" + email + "/rating?score=" + rating,
                null,
                Void.class
        );
    }

    @Override
    public UUID findUserIdByUsername(String username) {
        return restTemplate.getForObject(
                authServiceUrl + "/api/v1/users/username/" + username + "/id",
                UUID.class
        );
    }
}