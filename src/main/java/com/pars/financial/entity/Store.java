package com.pars.financial.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
    @Index(name = "idx_store_name", columnList = "store_name"),
    @Index(name = "idx_store_company", columnList = "company_id")
})
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "phone_number_id")
    private PhoneNumber phone_number;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    private String store_name;

    public PhoneNumber getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(PhoneNumber phone_number) {
        this.phone_number = phone_number;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }
}
