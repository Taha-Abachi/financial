package com.pars.financial.dto;

import jakarta.validation.constraints.NotBlank;

public class CompanyUpdateRequest {
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    private String phoneNumber;
    private String address;

    public CompanyUpdateRequest() {
    }

    public CompanyUpdateRequest(String companyName, String phoneNumber, String address) {
        this.companyName = companyName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
