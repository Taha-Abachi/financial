package com.pars.financial.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.entity.Customer;
import com.pars.financial.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Cacheable(value = "customers", key = "#phoneNumber")
    public Customer getCustomer(String phoneNumber) {
        return customerRepository.findByPrimaryPhoneNumber(phoneNumber);
    }

    @Transactional
    public Customer createCustomer(String phoneNumber) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.phoneNumber = phoneNumber;
        return createCustomer(customerDto);
    }

    @Transactional
    @CachePut(value = "customers", key = "#customerDto.phoneNumber")
    public Customer createCustomer(CustomerDto customerDto) {
        var customer = customerRepository.findByPrimaryPhoneNumber(customerDto.phoneNumber);
        if (customer == null) {
            customer = new Customer();
            customer.setName(customerDto.name);
            customer.setSurname(customerDto.surName);
            customer.setPrimaryPhoneNumber(customerDto.phoneNumber);
            customerRepository.save(customer);
        }
        return customer;
    }
}
