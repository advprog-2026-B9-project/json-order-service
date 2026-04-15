package com.b9.json.jsonplatform.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String username;

    private String fullName;
    private String role = "TITIPERS";
    private String phoneNumber;
    private String address;

    private String kycStatus = "UNVERIFIED";
    private String nikKtp;
    private String ktpImageUrl;
}