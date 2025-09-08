package com.pars.financial.entity;

import java.time.LocalDateTime;


import com.pars.financial.utils.ApiKeyEncryption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_mobile", columnList = "mobilePhoneNumber"),
    @Index(name = "idx_user_national_code", columnList = "nationalCode"),
    @Index(name = "idx_user_api_key", columnList = "apiKey")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Password cannot be blank")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "Mobile phone number cannot be blank")
    @Size(min = 10, max = 15, message = "Mobile phone number must be between 10 and 15 characters")
    @Column(name = "mobile_phone_number", nullable = false, unique = true)
    private String mobilePhoneNumber;

    @NotBlank(message = "National code cannot be blank")
    @Size(min = 8, max = 20, message = "National code must be between 8 and 20 characters")
    @Column(name = "national_code", nullable = false, unique = true)
    private String nationalCode;

    @Column(name = "email")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull(message = "User role cannot be null")
    private UserRole role;

    @Column(name = "api_key", columnDefinition = "TEXT")
    private String apiKey;

    @Transient
    private static ApiKeyEncryption apiKeyEncryption;

    public static void setApiKeyEncryption(ApiKeyEncryption encryption) {
        apiKeyEncryption = encryption;
    }

    public User() {}

    public User(String username, String name, String password, String mobilePhoneNumber, String nationalCode, UserRole role) {
        this.username = username;
        this.name = name;
        this.password = password; // Store raw password, will be encoded by service
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.nationalCode = nationalCode;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password; // Store raw password, encoding will be done by service
    }

    public void setEncodedPassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean matchesPassword(String rawPassword, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getApiKey() {
        return apiKeyEncryption != null && (apiKey != null) && (!apiKey.isEmpty())  ? apiKeyEncryption.decrypt(apiKey) : "";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKeyEncryption != null ? apiKeyEncryption.encrypt(apiKey) : apiKey;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public boolean canUseApiKey() {
        return role != null && (com.pars.financial.enums.UserRole.API_USER.name().equals(role.getName()) || 
                               com.pars.financial.enums.UserRole.ADMIN.name().equals(role.getName()) ||
                               com.pars.financial.enums.UserRole.SUPERADMIN.name().equals(role.getName()));
    }
} 