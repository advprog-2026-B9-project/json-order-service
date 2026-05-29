package com.b9.json.jsonplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class JsonPlatformApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        setIfPresent("spring.datasource.url", dotenv.get("DB_URL", null));
        setIfPresent("spring.datasource.username", dotenv.get("DB_USERNAME", null));
        setIfPresent("spring.datasource.password", dotenv.get("DB_PASSWORD", null));

        SpringApplication.run(JsonPlatformApplication.class, args);
    }

    private static void setIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}