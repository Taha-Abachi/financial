package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.dto.CustomerUpdateRequest;
import com.pars.financial.entity.Customer;
import com.pars.financial.entity.User;
import com.pars.financial.entity.UserRole;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.repository.UserRoleRepository;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, 
                          UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
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

    public Customer getCustomerById(Long id) {
        logger.debug("Fetching customer with ID: {}", id);
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) {
            logger.warn("Customer not found with ID: {}", id);
            throw new CustomerNotFoundException("Customer not found with ID: " + id);
        }
        return customer.get();
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
        
        // Ensure user exists for this customer
        ensureUserExistsForCustomer(customer);
        
        return customer;
    }

    /**
     * Update an existing customer
     * @param id the customer ID
     * @param request the update request
     * @return the updated customer
     * @throws CustomerNotFoundException if customer not found
     */
    @Transactional
    @CacheEvict(value = "customers", key = "#result.primaryPhoneNumber")
    public Customer updateCustomer(Long id, CustomerUpdateRequest request) {
        logger.info("Updating customer with ID: {}", id);
        
        Customer customer = getCustomerById(id);
        
        // Update fields if provided
        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        
        if (request.getSurname() != null) {
            customer.setSurname(request.getSurname());
        }
        
        if (request.getPrimaryPhoneNumber() != null) {
            // Check if phone number is already taken by another customer
            Customer existingCustomer = customerRepository.findByPrimaryPhoneNumber(request.getPrimaryPhoneNumber());
            if (existingCustomer != null && !existingCustomer.getId().equals(id)) {
                logger.warn("Phone number {} already exists for another customer", request.getPrimaryPhoneNumber());
                throw new ValidationException(ErrorCodes.CUSTOMER_ALREADY_EXISTS, "Phone number already exists for another customer");
            }
            customer.setPrimaryPhoneNumber(request.getPrimaryPhoneNumber());
        }
        
        if (request.getNationalCode() != null) {
            customer.setNationalCode(request.getNationalCode());
        }
        
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Updated customer with ID: {}", savedCustomer.getId());
        
        // Ensure user exists for this customer (in case phone number changed or user was deleted)
        ensureUserExistsForCustomer(savedCustomer);
        
        return savedCustomer;
    }

    /**
     * Ensure a user exists for the given customer
     * Creates a user if one doesn't exist, matching by phone number
     * Updates existing user's phone number if customer's phone number changed
     * @param customer the customer
     */
    @Transactional
    private void ensureUserExistsForCustomer(Customer customer) {
        if (customer.getPrimaryPhoneNumber() == null || customer.getPrimaryPhoneNumber().trim().isEmpty()) {
            logger.warn("Customer {} has no phone number, cannot create user", customer.getId());
            return;
        }
        
        // Check if user already exists for this phone number
        Optional<User> existingUser = userRepository.findByMobilePhoneNumber(customer.getPrimaryPhoneNumber());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user information from customer if needed
            boolean updated = false;
            if (customer.getName() != null && !customer.getName().equals(user.getName())) {
                user.setName(customer.getName());
                updated = true;
            }
            if (customer.getEmail() != null && !customer.getEmail().equals(user.getEmail())) {
                user.setEmail(customer.getEmail());
                updated = true;
            }
            if (customer.getNationalCode() != null && !customer.getNationalCode().equals(user.getNationalCode())) {
                user.setNationalCode(customer.getNationalCode());
                updated = true;
            }
            if (updated) {
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                logger.debug("Updated user {} for customer {} with phone number: {}", 
                            user.getUsername(), customer.getId(), customer.getPrimaryPhoneNumber());
            } else {
                logger.debug("User already exists for customer {} with phone number: {}", 
                            customer.getId(), customer.getPrimaryPhoneNumber());
            }
            return;
        }
        
        // Check if there's a user with a different phone number that might be associated with this customer
        // This handles the case where customer phone number was updated
        // We search for users with username pattern "customer_*" that might be related
        // Note: This is a best-effort approach. In a production system, you might want
        // to maintain a direct relationship between Customer and User entities.
        
        // Get USER role (regular user role for customers)
        Optional<UserRole> userRole = userRoleRepository.findByName("USER");
        if (userRole.isEmpty()) {
            logger.warn("USER role not found, cannot create user for customer {}", customer.getId());
            throw new ValidationException(ErrorCodes.USER_ROLE_NOT_FOUND, "USER role not found in system");
        }
        
        // Generate username from phone number (or use phone number itself)
        String username = "customer_" + customer.getPrimaryPhoneNumber();
        // Ensure username is unique
        int suffix = 1;
        String originalUsername = username;
        while (userRepository.existsByUsername(username)) {
            username = originalUsername + "_" + suffix;
            suffix++;
        }
        
        // Generate a default password (could be phone number or a random string)
        // In production, you might want to send a password reset link instead
        String defaultPassword = customer.getPrimaryPhoneNumber(); // Using phone number as default password
        
        // Generate national code if not provided (use phone number as fallback)
        String nationalCode = customer.getNationalCode();
        if (nationalCode == null || nationalCode.trim().isEmpty()) {
            nationalCode = customer.getPrimaryPhoneNumber(); // Fallback to phone number
        }
        
        // Create user
        User user = new User(
            username,
            customer.getName() != null ? customer.getName() : "Customer",
            defaultPassword,
            customer.getPrimaryPhoneNumber(),
            nationalCode,
            userRole.get()
        );
        
        // Set additional fields
        if (customer.getEmail() != null) {
            user.setEmail(customer.getEmail());
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        
        // Encode password
        user.setEncodedPassword(passwordEncoder.encode(defaultPassword));
        
        // Note: USER role cannot use API keys, so we don't generate one
        
        User savedUser = userRepository.save(user);
        logger.info("Created user {} for customer {} with phone number: {}", 
                    savedUser.getUsername(), customer.getId(), customer.getPrimaryPhoneNumber());
    }
}
