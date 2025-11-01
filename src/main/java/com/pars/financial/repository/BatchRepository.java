package com.pars.financial.repository;

import com.pars.financial.entity.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByBatchNumber(String batchNumber);
    boolean existsByBatchNumber(String batchNumber);
    List<Batch> findByBatchType(Batch.BatchType batchType);
    List<Batch> findByStatus(Batch.BatchStatus status);
    List<Batch> findByCompanyId(Long companyId);
    List<Batch> findByRequestUserId(Long requestUserId);
    List<Batch> findByCompanyIdAndBatchType(Long companyId, Batch.BatchType batchType);
    List<Batch> findByCompanyIdAndStatus(Long companyId, Batch.BatchStatus status);
    
    // Pagination methods
    // findAll(Pageable) is already provided by JpaRepository, no need to redeclare
    Page<Batch> findByCompanyId(Long companyId, Pageable pageable);
    Page<Batch> findByRequestUserId(Long requestUserId, Pageable pageable);
    Page<Batch> findByBatchType(Batch.BatchType batchType, Pageable pageable);
    Page<Batch> findByStatus(Batch.BatchStatus status, Pageable pageable);
} 