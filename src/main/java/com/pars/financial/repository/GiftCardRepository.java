package com.pars.financial.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pars.financial.entity.Company;
import com.pars.financial.entity.GiftCard;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    GiftCard findBySerialNo(String serialNo);
    GiftCard findByIdentifier(Long identifier);
    List<GiftCard> findByCompany(Company company);
    Page<GiftCard> findByCompany(Company company, Pageable pageable);
    List<GiftCard> findByBatchId(Long batchId);
    
    // Statistics queries
    @Query("SELECT COUNT(g) FROM GiftCard g")
    Long countAllGiftCards();
    
    @Query("SELECT COALESCE(SUM(g.balance), 0) FROM GiftCard g")
    Long sumTotalBalance();
    
    @Query("SELECT COALESCE(SUM(g.initialAmount), 0) FROM GiftCard g")
    Long sumTotalInitialAmount();
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.isActive = true")
    Long countActiveGiftCards();
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.blocked = true")
    Long countBlockedGiftCards();
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.expiryDate < :currentDate")
    Long countExpiredGiftCards(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.balance < g.initialAmount")
    Long countUsedGiftCards();
    
    @Query("SELECT COALESCE(AVG(g.balance), 0) FROM GiftCard g")
    Double getAverageBalance();
    
    @Query("SELECT COALESCE(AVG(g.initialAmount), 0) FROM GiftCard g")
    Double getAverageInitialAmount();
    
    // Company-specific statistics
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.company.id = :companyId")
    Long countGiftCardsByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(SUM(g.balance), 0) FROM GiftCard g WHERE g.company.id = :companyId")
    Long sumBalanceByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(SUM(g.initialAmount), 0) FROM GiftCard g WHERE g.company.id = :companyId")
    Long sumInitialAmountByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.company.id = :companyId AND g.isActive = true")
    Long countActiveGiftCardsByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.company.id = :companyId AND g.blocked = true")
    Long countBlockedGiftCardsByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.company.id = :companyId AND g.expiryDate < :currentDate")
    Long countExpiredGiftCardsByCompany(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(g) FROM GiftCard g WHERE g.company.id = :companyId AND g.balance < g.initialAmount")
    Long countUsedGiftCardsByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(g.balance), 0) FROM GiftCard g WHERE g.company.id = :companyId")
    Double getAverageBalanceByCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT COALESCE(AVG(g.initialAmount), 0) FROM GiftCard g WHERE g.company.id = :companyId")
    Double getAverageInitialAmountByCompany(@Param("companyId") Long companyId);
}
