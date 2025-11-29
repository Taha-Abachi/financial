package com.pars.financial.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.Company;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

@Repository
public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Integer> {
    GiftCardTransaction findByTransactionId(UUID transactionId);

    GiftCardTransaction findByTransactionTypeAndTransactionId(TransactionType transactionType, UUID transactionId);

    GiftCardTransaction findByClientTransactionId(String clientTransactionId);
    
    GiftCardTransaction findByTransactionTypeAndClientTransactionId(TransactionType transactionType, String clientTransactionId);

    List<GiftCardTransaction> findByGiftCardAndTransactionType(GiftCard giftCard, TransactionType transactionType);
    Page<GiftCardTransaction> findByGiftCardAndTransactionType(GiftCard giftCard, TransactionType transactionType, Pageable pageable);

    List<GiftCardTransaction> findByGiftCard(GiftCard giftCard);

    GiftCardTransaction findTopByGiftCardOrderByTrxDateDesc(GiftCard giftCard);

    GiftCardTransaction findTopByTransactionIdOrderByTrxDateDesc(UUID transactionId);
    
    // Paginated method for company transaction history
    @Query("SELECT t FROM GiftCardTransaction t WHERE t.giftCard.company = :company")
    Page<GiftCardTransaction> findByGiftCardCompany(@Param("company") Company company, Pageable pageable);
    
    // New methods for data cleansing
    List<GiftCardTransaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);
    
    List<GiftCardTransaction> findByTransactionType(TransactionType transactionType);
    
    // Statistics methods for gift card reports
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM GiftCardTransaction t WHERE t.transactionType = 'Debit' AND t.status = 'Confirmed'")
    Long countDebitTransactions();
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.Amount), 0) FROM GiftCardTransaction t WHERE t.transactionType = 'Debit' AND t.status = 'Confirmed'")
    Long sumDebitAmount();
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM GiftCardTransaction t WHERE t.transactionType = 'Debit' AND t.status = 'Confirmed' AND t.giftCard.company.id = :companyId")
    Long countDebitTransactionsByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(t.Amount), 0) FROM GiftCardTransaction t WHERE t.transactionType = 'Debit' AND t.status = 'Confirmed' AND t.giftCard.company.id = :companyId")
    Long sumDebitAmountByCompany(@org.springframework.data.repository.query.Param("companyId") Long companyId);
    
    // Store-specific methods
    List<GiftCardTransaction> findByStoreId(Long storeId);
    
    List<GiftCardTransaction> findByStoreIdAndGiftCardSerialNo(Long storeId, String serialNo);
    
    // Store-specific methods - confirmed debit transactions only
    @Query("SELECT t FROM GiftCardTransaction t WHERE t.store.id = :storeId AND t.transactionType = 'Debit' AND t.status = 'Confirmed'")
    List<GiftCardTransaction> findConfirmedDebitTransactionsByStoreId(@Param("storeId") Long storeId);
    
    // Company-specific methods - get confirmed debit transactions from all stores belonging to a company
    @Query("SELECT t FROM GiftCardTransaction t WHERE t.store.company.id = :companyId AND t.transactionType = 'Debit' AND t.status = 'Confirmed'")
    List<GiftCardTransaction> findConfirmedDebitTransactionsByStoreCompanyId(@Param("companyId") Long companyId);
    
    // All confirmed debit transactions (for SUPERADMIN)
    @Query("SELECT t FROM GiftCardTransaction t WHERE t.transactionType = 'Debit' AND t.status = 'Confirmed'")
    List<GiftCardTransaction> findAllConfirmedDebitTransactions();
    
    // Aggregation methods for transaction statistics
    @Query("SELECT t.status, COUNT(t), COALESCE(SUM(t.Amount), 0), COALESCE(SUM(t.orderAmount), 0) " +
           "FROM GiftCardTransaction t " +
           "WHERE t.trxDate >= :startDate AND t.trxDate < :endDate " +
           "AND t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "GROUP BY t.status")
    List<Object[]> getTransactionAggregationsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t.status, COUNT(t), COALESCE(SUM(t.Amount), 0), COALESCE(SUM(t.orderAmount), 0) " +
           "FROM GiftCardTransaction t " +
           "WHERE t.trxDate >= :startDate AND t.trxDate < :endDate " +
           "AND t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.store.id = :storeId " +
           "GROUP BY t.status")
    List<Object[]> getTransactionAggregationsByDateRangeAndStore(@Param("startDate") LocalDateTime startDate, 
                                                                 @Param("endDate") LocalDateTime endDate,
                                                                 @Param("storeId") Long storeId);
    
    @Query("SELECT t.status, COUNT(t), COALESCE(SUM(t.Amount), 0), COALESCE(SUM(t.orderAmount), 0) " +
           "FROM GiftCardTransaction t " +
           "WHERE t.trxDate >= :startDate AND t.trxDate < :endDate " +
           "AND t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.giftCard.company.id = :companyId " +
           "GROUP BY t.status")
    List<Object[]> getTransactionAggregationsByDateRangeAndCompany(@Param("startDate") LocalDateTime startDate, 
                                                                   @Param("endDate") LocalDateTime endDate,
                                                                   @Param("companyId") Long companyId);
    
    // Settlement report queries - fetch fulfilled transactions with relationships
    @Query("SELECT t FROM GiftCardTransaction t " +
           "LEFT JOIN FETCH t.giftCard gc " +
           "LEFT JOIN FETCH gc.company " +
           "LEFT JOIN FETCH t.store s " +
           "LEFT JOIN FETCH s.company " +
           "LEFT JOIN FETCH t.customer " +
           "WHERE t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.trxDate >= :startDate " +
           "AND t.trxDate < :endDate")
    List<GiftCardTransaction> findFulfilledTransactionsForSettlement(@Param("startDate") LocalDateTime startDate,
                                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM GiftCardTransaction t " +
           "LEFT JOIN FETCH t.giftCard gc " +
           "LEFT JOIN FETCH gc.company " +
           "LEFT JOIN FETCH t.store s " +
           "LEFT JOIN FETCH s.company " +
           "LEFT JOIN FETCH t.customer " +
           "WHERE t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.trxDate >= :startDate " +
           "AND t.trxDate < :endDate " +
           "AND t.store.id = :storeId")
    List<GiftCardTransaction> findFulfilledTransactionsForSettlementByStore(@Param("startDate") LocalDateTime startDate,
                                                                           @Param("endDate") LocalDateTime endDate,
                                                                           @Param("storeId") Long storeId);
    
    @Query("SELECT t FROM GiftCardTransaction t " +
           "LEFT JOIN FETCH t.giftCard gc " +
           "LEFT JOIN FETCH gc.company " +
           "LEFT JOIN FETCH t.store s " +
           "LEFT JOIN FETCH s.company " +
           "LEFT JOIN FETCH t.customer " +
           "WHERE t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.trxDate >= :startDate " +
           "AND t.trxDate < :endDate " +
           "AND gc.company.id = :companyId")
    List<GiftCardTransaction> findFulfilledTransactionsForSettlementByIssuerCompany(@Param("startDate") LocalDateTime startDate,
                                                                                   @Param("endDate") LocalDateTime endDate,
                                                                                   @Param("companyId") Long companyId);
    
    @Query("SELECT t FROM GiftCardTransaction t " +
           "LEFT JOIN FETCH t.giftCard gc " +
           "LEFT JOIN FETCH gc.company " +
           "LEFT JOIN FETCH t.store s " +
           "LEFT JOIN FETCH s.company " +
           "LEFT JOIN FETCH t.customer " +
           "WHERE t.transactionType = 'Debit' " +
           "AND t.status = 'Confirmed' " +
           "AND t.trxDate >= :startDate " +
           "AND t.trxDate < :endDate " +
           "AND s.company.id = :companyId")
    List<GiftCardTransaction> findFulfilledTransactionsForSettlementByStoreCompany(@Param("startDate") LocalDateTime startDate,
                                                                                   @Param("endDate") LocalDateTime endDate,
                                                                                   @Param("companyId") Long companyId);
}
