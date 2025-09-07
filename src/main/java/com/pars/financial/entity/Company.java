package com.pars.financial.entity;

import jakarta.persistence.*;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "phone_number_id")
    private PhoneNumber phone_number;

    @ManyToOne
    @JoinColumn(name = "company_address_id")
    private Address company_address;

    public PhoneNumber getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(PhoneNumber phone_number) {
        this.phone_number = phone_number;
    }

    public Address getCompany_address() {
        return company_address;
    }

    public void setCompany_address(Address company_address) {
        this.company_address = company_address;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String company_name) {
        this.name = company_name;
    }


}
