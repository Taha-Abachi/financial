package com.pars.financial.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.pars.financial.enums.UserRole;
import com.pars.financial.utils.ApiKeyEncryption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class ApiUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(columnDefinition = "TEXT")
    private String apiKey;

    @Transient
    private static ApiKeyEncryption apiKeyEncryption;

    @Transient
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void setApiKeyEncryption(ApiKeyEncryption encryption) {
        apiKeyEncryption = encryption;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = passwordEncoder.encode(password);
    }

    public boolean matchesPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public String getApiKey() {
        return apiKeyEncryption != null ? apiKeyEncryption.decrypt(apiKey) : apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKeyEncryption != null ? apiKeyEncryption.encrypt(apiKey) : apiKey;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
