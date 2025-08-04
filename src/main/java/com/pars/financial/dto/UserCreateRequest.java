package com.pars.financial.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Mobile phone number cannot be blank")
    @Size(min = 10, max = 15, message = "Mobile phone number must be between 10 and 15 characters")
    private String mobilePhoneNumber;

    @NotBlank(message = "National code cannot be blank")
    @Size(min = 8, max = 20, message = "National code must be between 8 and 20 characters")
    private String nationalCode;

    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Role ID cannot be null")
    private Long roleId;

    public UserCreateRequest() {}

    public UserCreateRequest(String username, String name, String password, String mobilePhoneNumber, String nationalCode, String email, Long roleId) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.nationalCode = nationalCode;
        this.email = email;
        this.roleId = roleId;
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
        this.password = password;
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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
} 