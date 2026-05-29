package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AuthServiceClientImpl implements AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.auth.url}")
    private String authServiceUrl;

    public String findEmailById(UUID userId) {
        String url = authServiceUrl + "/api/v1/auth/list";
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null) throw new IllegalArgumentException("User tidak ditemukan");

        return users.stream()
                .filter(u -> userId.toString().equals(String.valueOf(u.get("id"))))
                .map(u -> (String) u.get("email"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
    }

    @Override
    public void addRating(String email, int rating) {
        String url = UriComponentsBuilder
                .fromHttpUrl(authServiceUrl + "/api/v1/auth/rating")
                .queryParam("jastiperEmail", email)
                .queryParam("ratingScore", rating)
                .build()
                .toUriString();
        restTemplate.postForObject(url, null, Void.class);
    }

    @Override
    public UUID findUserIdByUsername(String username) {
        UserDto[] users = restTemplate.getForObject(
                authServiceUrl + "/api/v1/auth/list",
                UserDto[].class
        );
        if (users == null) throw new IllegalArgumentException("Gagal fetch users dari auth-service");
        return java.util.Arrays.stream(users)
                .filter(u -> u.getUsername() != null && u.getUsername().equals(username))
                .map(UserDto::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
    }

    @Getter @Setter
    public static class UserInternalDto {
        private UUID id;
        private String email;
        private String username;
        private String fullName;
        private String phoneNumber;
    }

    @Getter @Setter
    public static class UserDto {
        private UUID id;
        private String email;
        private String username;
        private String fullName;
        private String role;
    }
}