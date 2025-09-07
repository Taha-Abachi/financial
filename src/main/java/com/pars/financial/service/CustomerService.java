package com.pars.financial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.entity.Customer;
import com.pars.financial.repository.CustomerRepository;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Cacheable(value = "customers", key = "#phoneNumber")
    public Customer getCustomer(String phoneNumber) {
        logger.debug("Fetching customer with phone number: {}", phoneNumber);
        var customer = customerRepository.findByPrimaryPhoneNumber(phoneNumber);
        if (customer == null) {
            logger.warn("Customer not found with phone number: {}", phoneNumber);
        }
        return customer;
    }

    @Transactional
    public Customer createCustomer(String phoneNumber) {
        logger.info("Creating new customer with phone number: {}", phoneNumber);
        CustomerDto customerDto = new CustomerDto();
        customerDto.phoneNumber = phoneNumber;
        return createCustomer(customerDto);
    }

    @Transactional
    @CachePut(value = "customers", key = "#customerDto.phoneNumber")
    public Customer createCustomer(CustomerDto customerDto) {
        logger.info("Creating/updating customer with phone number: {}", customerDto.phoneNumber);
        var customer = customerRepository.findByPrimaryPhoneNumber(customerDto.phoneNumber);
        if (customer == null) {
            logger.debug("Creating new customer record for phone number: {}", customerDto.phoneNumber);
            customer = new Customer();
            customer.setName(customerDto.name);
            customer.setSurname(customerDto.surName);
            customer.setPrimaryPhoneNumber(customerDto.phoneNumber);
            customer = customerRepository.save(customer);
            logger.info("Created new customer with ID: {}", customer.getId());
        } else {
            logger.debug("Customer already exists with phone number: {}", customerDto.phoneNumber);
        }
        return customer;
    }
}
