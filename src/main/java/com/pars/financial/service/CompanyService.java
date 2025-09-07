package com.pars.financial.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pars.financial.entity.Company;
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
            throw new ValidationException("Company not found", null, -134);
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
     * @param company the company to create
     * @return the created company
     */
    public Company createCompany(Company company) {
        logger.info("Creating new company: {}", company.getName());
        return companyRepository.save(company);
    }

    /**
     * Update an existing company
     * @param company the company to update
     * @return the updated company
     */
    public Company updateCompany(Company company) {
        logger.info("Updating company: {}", company.getName());
        if (company.getId() == null) {
            throw new ValidationException("Company ID is required for update", null, -135);
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
            throw new ValidationException("Company not found", null, -134);
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