package com.pars.financial.dto;

import java.time.LocalDateTime;

public class UserInfoDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String mobilePhoneNumber;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRoleInfoDto role;
    private Long storeId;
    private String storeName;
    private Long companyId;
    private String companyName;

    public UserInfoDto() {}

    public UserInfoDto(Long id, String username, String name, String email, String mobilePhoneNumber, 
                      boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, UserRoleInfoDto role,
                      Long storeId, String storeName, Long companyId, String companyName) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.role = role;
        this.storeId = storeId;
        this.storeName = storeName;
        this.companyId = companyId;
        this.companyName = companyName;
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
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

    public UserRoleInfoDto getRole() {
        return role;
    }

    public void setRole(UserRoleInfoDto role) {
        this.role = role;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // Inner class for role information
    public static class UserRoleInfoDto {
        private Long id;
        private String name;
        private String description;

        public UserRoleInfoDto() {}

        public UserRoleInfoDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
