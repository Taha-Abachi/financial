package com.pars.financial.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.CompanyCreateRequest;
import com.pars.financial.dto.CompanyUpdateRequest;
import com.pars.financial.entity.Address;
import com.pars.financial.entity.Company;
import com.pars.financial.entity.PhoneNumber;
import com.pars.financial.enums.PhoneNumberType;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.CompanyRepository;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Find company by ID
     * @param companyId the company ID
     * @return the company if found
     * @throws ValidationException if company not found
     */
    public Company findById(Long companyId) {
        logger.debug("Finding company by ID: {}", companyId);
        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            logger.warn("Company not found with ID: {}", companyId);
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }
        return company.get();
    }

    /**
     * Find company by name
     * @param companyName the company name
     * @return the company if found, null otherwise
     */
    public Company findByName(String companyName) {
        logger.debug("Finding company by name: {}", companyName);
        return companyRepository.findByName(companyName);
    }

    /**
     * Get all companies
     * @return list of all companies
     */
    public List<Company> getAllCompanies() {
        logger.debug("Fetching all companies");
        return companyRepository.findAll();
    }

    /**
     * Create a new company
     * @param request the company creation request
     * @return the created company
     */
    @Transactional
    public Company createCompany(CompanyCreateRequest request) {
        logger.info("Creating new company: {}", request.getCompanyName());
        
        Company company = new Company();
        company.setName(request.getCompanyName());
        
        // Create phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber(request.getPhoneNumber());
            phoneNumber.setType(PhoneNumberType.Cell); // Default type
            company.setPhone_number(phoneNumber);
        }
        
        // Create address if provided
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            Address address = new Address();
            address.setText(request.getAddress());
            address.setCity("Unknown"); // Default values
            address.setProvince("Unknown");
            address.setPostalCode("00000");
            company.setCompany_address(address);
        }
        
        return companyRepository.save(company);
    }

    /**
     * Update an existing company
     * @param companyId the company ID to update
     * @param request the company update request
     * @return the updated company
     */
    @Transactional
    public Company updateCompany(Long companyId, CompanyUpdateRequest request) {
        logger.info("Updating company with ID: {}", companyId);
        
        Company company = findById(companyId);
        company.setName(request.getCompanyName());
        
        // Update phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            PhoneNumber phoneNumber = company.getPhone_number();
            if (phoneNumber == null) {
                phoneNumber = new PhoneNumber();
                phoneNumber.setType(PhoneNumberType.Cell); // Default type
            }
            phoneNumber.setNumber(request.getPhoneNumber());
            company.setPhone_number(phoneNumber);
        }
        
        // Update address if provided
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            Address address = company.getCompany_address();
            if (address == null) {
                address = new Address();
                address.setCity("Unknown"); // Default values
                address.setProvince("Unknown");
                address.setPostalCode("00000");
            }
            address.setText(request.getAddress());
            company.setCompany_address(address);
        }
        
        return companyRepository.save(company);
    }

    /**
     * Delete a company
     * @param companyId the company ID to delete
     */
    public void deleteCompany(Long companyId) {
        logger.info("Deleting company with ID: {}", companyId);
        if (!companyRepository.existsById(companyId)) {
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }
        companyRepository.deleteById(companyId);
    }

    /**
     * Check if company exists by name
     * @param companyName the company name to check
     * @return true if company exists, false otherwise
     */
    public boolean existsByName(String companyName) {
        return companyRepository.existsByName(companyName);
    }
} 