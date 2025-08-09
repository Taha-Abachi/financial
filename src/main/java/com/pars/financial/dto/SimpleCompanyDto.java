package com.pars.financial.dto;

import com.pars.financial.entity.Company;

public class SimpleCompanyDto {
    private Long id;
    private String name;

    public SimpleCompanyDto() {}

    public SimpleCompanyDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SimpleCompanyDto fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        return new SimpleCompanyDto(company.getId(), company.getName());
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
} 