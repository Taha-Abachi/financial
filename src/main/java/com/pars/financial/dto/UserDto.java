package com.pars.financial.dto;

import com.pars.financial.entity.User;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String username;
    private String name;
    private String mobilePhoneNumber;
    private String nationalCode;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRoleDto role;

    public UserDto() {}

    public UserDto(Long id, String username, String name, String mobilePhoneNumber, String nationalCode, String email, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, UserRoleDto role) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.nationalCode = nationalCode;
        this.email = email;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.role = role;
    }

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getMobilePhoneNumber(),
            user.getNationalCode(),
            user.getEmail(),
            user.isActive(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            UserRoleDto.fromEntity(user.getRole())
        );
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

    public UserRoleDto getRole() {
        return role;
    }

    public void setRole(UserRoleDto role) {
        this.role = role;
    }
} 