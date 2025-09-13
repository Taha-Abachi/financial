package com.pars.financial.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pars.financial.entity.DiscountCode;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    public DiscountCode findByCode(String code);
    public boolean existsByCode(String code);
    public boolean existsBySerialNo(Long serialNo);
    public java.util.List<DiscountCode> findByBatchId(Long batchId);
    
    // Statistics queries
    @Query("SELECT COUNT(d) FROM DiscountCode d")
    Long countAllDiscountCodes();
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.used = true")
    Long countUsedDiscountCodes();
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.used = false")
    Long countUnusedDiscountCodes();
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.isActive = true")
    Long countActiveDiscountCodes();
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.isActive = false")
    Long countInactiveDiscountCodes();
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.expiryDate < :currentDate")
    Long countExpiredDiscountCodes(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COALESCE(AVG(d.percentage), 0) FROM DiscountCode d")
    Double getAveragePercentage();
    
    @Query("SELECT COALESCE(AVG(d.maxDiscountAmount), 0) FROM DiscountCode d")
    Double getAverageMaxDiscountAmount();
    
    @Query("SELECT COALESCE(AVG(d.minimumBillAmount), 0) FROM DiscountCode d")
    Double getAverageMinimumBillAmount();
    
    @Query("SELECT COALESCE(AVG(d.currentUsageCount), 0) FROM DiscountCode d")
    Double getAverageUsageCount();
    
    @Query("SELECT COALESCE(AVG(d.usageLimit), 0) FROM DiscountCode d")
    Double getAverageUsageLimit();
    
    // Company-specific statistics
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId")
    Long countDiscountCodesByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId AND d.used = true")
    Long countUsedDiscountCodesByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId AND d.used = false")
    Long countUnusedDiscountCodesByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId AND d.isActive = true")
    Long countActiveDiscountCodesByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId AND d.isActive = false")
    Long countInactiveDiscountCodesByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.company.id = :companyId AND d.expiryDate < :currentDate")
    Long countExpiredDiscountCodesByCompany(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COALESCE(AVG(d.percentage), 0) FROM DiscountCode d WHERE d.company.id = :companyId")
    Double getAveragePercentageByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(d.maxDiscountAmount), 0) FROM DiscountCode d WHERE d.company.id = :companyId")
    Double getAverageMaxDiscountAmountByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(d.minimumBillAmount), 0) FROM DiscountCode d WHERE d.company.id = :companyId")
    Double getAverageMinimumBillAmountByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(d.currentUsageCount), 0) FROM DiscountCode d WHERE d.company.id = :companyId")
    Double getAverageUsageCountByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(d.usageLimit), 0) FROM DiscountCode d WHERE d.company.id = :companyId")
    Double getAverageUsageLimitByCompany(@Param("companyId") Long companyId);
}
