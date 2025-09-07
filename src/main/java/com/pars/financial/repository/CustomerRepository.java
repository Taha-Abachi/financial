package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    public Customer findByPrimaryPhoneNumber(String phoneNumber);
}
