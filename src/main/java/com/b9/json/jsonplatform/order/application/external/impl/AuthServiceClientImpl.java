package com.b9.json.jsonplatform.order.application.external.impl;

import com.b9.json.jsonplatform.order.application.external.AuthServiceClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
        // GET /api/v1/auth/internal/user?id={userId}
        UserInternalDto user = restTemplate.getForObject(
                authServiceUrl + "/api/v1/auth/internal/user?id=" + userId,
                UserInternalDto.class
        );
        if (user == null) throw new IllegalArgumentException("User tidak ditemukan dengan id: " + userId);
        return user.getEmail();
    }

    @Override
    public void addRating(String email, int rating) {
        // POST /api/v1/auth/rating?jastiperEmail=&ratingScore=
        restTemplate.postForObject(
                authServiceUrl + "/api/v1/auth/rating?jastiperEmail="
                        + email + "&ratingScore=" + rating,
                null,
                Void.class
        );
    }

    @Override
    public UUID findUserIdByUsername(String username) {
        // GET /api/v1/auth/list → filter by username
        UserDto[] users = restTemplate.getForObject(
                authServiceUrl + "/api/v1/auth/list",
                UserDto[].class
        );
        if (users == null) throw new IllegalArgumentException("Gagal fetch users dari auth-service");
        return java.util.Arrays.stream(users)
                .filter(u -> username.equals(u.getUsername()))
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