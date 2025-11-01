package com.pars.financial.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.CompanyCreateRequest;
import com.pars.financial.dto.CompanyDto;
import com.pars.financial.dto.CompanyUpdateRequest;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.entity.Company;
import com.pars.financial.service.CompanyService;
import com.pars.financial.service.StoreService;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;
    private final StoreService storeService;

    public CompanyController(CompanyService companyService, StoreService storeService) {
        this.companyService = companyService;
        this.storeService = storeService;
    }

    /**
     * Get all companies with pagination
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 10000, max: 100)
     * @return paginated list of companies
     */
    @GetMapping
    public ResponseEntity<PagedResponse<CompanyDto>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10000") int size) {
        logger.info("Fetching companies with pagination - page: {}, size: {}", page, size);
        PagedResponse<CompanyDto> companies = companyService.getAllCompanies(page, size);
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
     * @param request the company creation request
     * @return the created company
     */
    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyCreateRequest request) {
        logger.info("Creating new company: {}", request.getCompanyName());
        Company createdCompany = companyService.createCompany(request);
        return ResponseEntity.ok(CompanyDto.fromEntity(createdCompany));
    }

    /**
     * Update an existing company
     * @param id the company ID
     * @param request the company update request
     * @return the updated company
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @RequestBody CompanyUpdateRequest request) {
        logger.info("Updating company with ID: {}", id);
        Company updatedCompany = companyService.updateCompany(id, request);
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

    /**
     * Get all stores for a specific company with pagination
     * @param companyId the company ID
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 10, max: 100)
     * @return paginated list of stores belonging to the company
     */
    @GetMapping("/{companyId}/stores")
    public ResponseEntity<PagedResponse<StoreDto>> getCompanyStores(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching stores for company ID: {} with pagination - page: {}, size: {}", companyId, page, size);
        
        // Verify company exists - findById throws ValidationException if not found
        try {
            companyService.findById(companyId);
        } catch (Exception e) {
            logger.warn("Company not found with ID: {}", companyId);
            return ResponseEntity.notFound().build();
        }
        
        PagedResponse<StoreDto> stores = storeService.getStoresByCompany(companyId, page, size);
        logger.info("Found {} stores for company {} (page {})", stores.getContent().size(), companyId, page);
        return ResponseEntity.ok(stores);
    }
} 