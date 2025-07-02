package com.pars.financial.dto;

import com.pars.financial.entity.Company;

public class CompanyDto {
    private Long id;
    private String companyName;
    private String phoneNumber;
    private String address;

    public CompanyDto() {
    }

    public CompanyDto(Long id, String companyName, String phoneNumber, String address) {
        this.id = id;
        this.companyName = companyName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public static CompanyDto fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        
        String phoneNumber = company.getPhone_number() != null ? company.getPhone_number().getNumber() : null;
        String address = company.getCompany_address() != null ? company.getCompany_address().getText() : null;
        
        return new CompanyDto(
            company.getId(),
            company.getName(),
            phoneNumber,
            address
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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