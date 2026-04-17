package com.b9.json.jsonplatform.auth.infrastructure.config;

import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.auth.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail(adminEmail) == null) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setUsername("adminadprob9");

                admin.setPassword(passwordEncoder.encode(adminPassword));

                admin.setFullName("Super Admin JSON");
                admin.setRole("ADMIN");
                admin.setKycStatus("VERIFIED");

                userRepository.save(admin);
                System.out.println("✅ Akun Admin berhasil di-seed ke database!");
            }
        };
    }
}