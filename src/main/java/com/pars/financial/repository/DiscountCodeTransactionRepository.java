package com.pars.financial.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.DiscountCode;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.enums.TransactionType;

public interface DiscountCodeTransactionRepository extends JpaRepository<DiscountCodeTransaction, Long> {
    
    DiscountCodeTransaction findByTransactionId(UUID transactionId);

    DiscountCodeTransaction findByDiscountCodeAndTrxType(DiscountCode discountCode, TransactionType trxType);

    DiscountCodeTransaction findByClientTransactionIdAndTrxType(String clientTransactionId, TransactionType trxType);

    DiscountCodeTransaction findByTransactionIdAndTrxType(UUID transactionId, TransactionType trxType);

    @Override
    @org.springframework.lang.NonNull
    Page<DiscountCodeTransaction> findAll(@org.springframework.lang.NonNull Pageable pageable);
    
    // Statistics methods for discount code reports
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem'")
    Long countRedeemTransactions();
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.discountAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem'")
    Long sumDiscountAmount();
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.originalAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem'")
    Long sumOriginalAmount();
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.discountCode.company.id = :companyId")
    Long countRedeemTransactionsByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.discountAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.discountCode.company.id = :companyId")
    Long sumDiscountAmountByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.originalAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.discountCode.company.id = :companyId")
    Long sumOriginalAmountByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    // Store-specific methods
    java.util.List<DiscountCodeTransaction> findByStoreId(Long storeId);
    
    java.util.List<DiscountCodeTransaction> findByStoreIdAndDiscountCode(Long storeId, String discountCode);
}
