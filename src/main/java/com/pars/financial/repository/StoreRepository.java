package com.pars.financial.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * Find all stores belonging to a specific company
     * @param companyId the company ID
     * @return list of stores for the company
     */
    List<Store> findByCompanyId(Long companyId);
    
    /**
     * Find all stores with company, address, and phone number eagerly fetched
     * @return list of all stores with relationships loaded
     */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.company LEFT JOIN FETCH s.address LEFT JOIN FETCH s.phone_number")
    List<Store> findAllWithRelationships();
    
    /**
     * Find all stores with company, address, and phone number eagerly fetched (paginated)
     * @param pageable pagination information
     * @return paginated list of all stores with relationships loaded
     */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.company LEFT JOIN FETCH s.address LEFT JOIN FETCH s.phone_number")
    Page<Store> findAllWithRelationships(Pageable pageable);
    
    /**
     * Find store by ID with company, address, and phone number eagerly fetched
     * @param id the store ID
     * @return store with relationships loaded
     */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.company LEFT JOIN FETCH s.address LEFT JOIN FETCH s.phone_number WHERE s.id = :id")
    Store findByIdWithRelationships(@Param("id") Long id);
    
    /**
     * Find stores by company ID with company, address, and phone number eagerly fetched (paginated)
     * @param companyId the company ID
     * @param pageable pagination information
     * @return paginated list of stores for the company with relationships loaded
     */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.company LEFT JOIN FETCH s.address LEFT JOIN FETCH s.phone_number WHERE s.company.id = :companyId")
    Page<Store> findByCompanyIdWithRelationships(@Param("companyId") Long companyId, Pageable pageable);
}
