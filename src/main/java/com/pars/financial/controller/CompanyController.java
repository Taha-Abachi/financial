package com.pars.financial.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.CompanyDto;
import com.pars.financial.entity.Company;
import com.pars.financial.service.CompanyService;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Get all companies
     * @return list of all companies
     */
    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        logger.info("Fetching all companies");
        List<CompanyDto> companies = companyService.getAllCompanies()
            .stream()
            .map(CompanyDto::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(companies);
    }

    /**
     * Get company by ID
     * @param id the company ID
     * @return the company
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        logger.info("Fetching company by ID: {}", id);
        Company company = companyService.findById(id);
        return ResponseEntity.ok(CompanyDto.fromEntity(company));
    }

    /**
     * Get company by name
     * @param name the company name
     * @return the company
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<CompanyDto> getCompanyByName(@PathVariable String name) {
        logger.info("Fetching company by name: {}", name);
        Company company = companyService.findByName(name);
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(CompanyDto.fromEntity(company));
    }

    /**
     * Create a new company
     * @param company the company to create
     * @return the created company
     */
    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody Company company) {
        logger.info("Creating new company: {}", company.getName());
        Company createdCompany = companyService.createCompany(company);
        return ResponseEntity.ok(CompanyDto.fromEntity(createdCompany));
    }

    /**
     * Update an existing company
     * @param id the company ID
     * @param company the company data to update
     * @return the updated company
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        logger.info("Updating company with ID: {}", id);
        company.setId(id);
        Company updatedCompany = companyService.updateCompany(company);
        return ResponseEntity.ok(CompanyDto.fromEntity(updatedCompany));
    }

    /**
     * Delete a company
     * @param id the company ID to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        logger.info("Deleting company with ID: {}", id);
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if company exists by name
     * @param name the company name to check
     * @return true if company exists, false otherwise
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        logger.info("Checking if company exists by name: {}", name);
        boolean exists = companyService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
} 