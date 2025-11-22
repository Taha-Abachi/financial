package com.pars.financial.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.DiscountCode;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.enums.TransactionType;

@Repository
public interface DiscountCodeTransactionRepository extends JpaRepository<DiscountCodeTransaction, Long> {
    
    DiscountCodeTransaction findByTransactionId(UUID transactionId);

    DiscountCodeTransaction findByDiscountCodeAndTrxType(DiscountCode discountCode, TransactionType trxType);

    DiscountCodeTransaction findByClientTransactionIdAndTrxType(String clientTransactionId, TransactionType trxType);

    DiscountCodeTransaction findByTransactionIdAndTrxType(UUID transactionId, TransactionType trxType);

    @Override
    @org.springframework.lang.NonNull
    Page<DiscountCodeTransaction> findAll(@org.springframework.lang.NonNull Pageable pageable);
    
    // Statistics methods for discount code reports
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    Long countRedeemTransactions();
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.discountAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    Long sumDiscountAmount();
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.originalAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    Long sumOriginalAmount();
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed' AND t.discountCode.company.id = :companyId")
    Long countRedeemTransactionsByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.discountAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed' AND t.discountCode.company.id = :companyId")
    Long sumDiscountAmountByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.originalAmount), 0) FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed' AND t.discountCode.company.id = :companyId")
    Long sumOriginalAmountByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    // Store-specific methods
    java.util.List<DiscountCodeTransaction> findByStoreId(Long storeId);
    
    Page<DiscountCodeTransaction> findByStoreId(Long storeId, Pageable pageable);
    
    // Store-specific methods - confirmed redeem transactions only
    @org.springframework.data.jpa.repository.Query("SELECT t FROM DiscountCodeTransaction t WHERE t.store.id = :storeId AND t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    java.util.List<DiscountCodeTransaction> findConfirmedRedeemTransactionsByStoreId(@org.springframework.data.repository.query.Param("storeId") Long storeId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM DiscountCodeTransaction t WHERE t.store.company.id = :companyId")
    Page<DiscountCodeTransaction> findByCompanyId(@org.springframework.data.repository.query.Param("companyId") Long companyId, Pageable pageable);
    
    // Company-specific methods - get confirmed redeem transactions from all stores belonging to a company
    @org.springframework.data.jpa.repository.Query("SELECT t FROM DiscountCodeTransaction t WHERE t.store.company.id = :companyId AND t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    java.util.List<DiscountCodeTransaction> findConfirmedRedeemTransactionsByStoreCompanyId(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    // All confirmed redeem transactions (for SUPERADMIN)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM DiscountCodeTransaction t WHERE t.trxType = 'Redeem' AND t.status = 'Confirmed'")
    java.util.List<DiscountCodeTransaction> findAllConfirmedRedeemTransactions();
    
    java.util.List<DiscountCodeTransaction> findByStoreIdAndDiscountCodeCode(Long storeId, String discountCode);
}
