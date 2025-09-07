package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    /**
     * Find company by name
     * @param companyName the company name to search for
     * @return the company if found, null otherwise
     */
    Company findByName(String companyName);
    
    /**
     * Check if company exists by name
     * @param companyName the company name to check
     * @return true if company exists, false otherwise
     */
    boolean existsByName(String companyName);
} 