package com.pars.financial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * Find all stores belonging to a specific company
     * @param companyId the company ID
     * @return list of stores for the company
     */
    List<Store> findByCompanyId(Long companyId);
}
