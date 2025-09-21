package com.pars.financial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.StoreTransactionSummary;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.repository.DiscountCodeTransactionRepository;
import com.pars.financial.repository.GiftCardTransactionRepository;
import com.pars.financial.repository.StoreRepository;

@Service
public class StoreUserService {

    private static final Logger logger = LoggerFactory.getLogger(StoreUserService.class);

    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final DiscountCodeTransactionRepository discountCodeTransactionRepository;
    private final StoreRepository storeRepository;

    public StoreUserService(GiftCardTransactionRepository giftCardTransactionRepository,
                           DiscountCodeTransactionRepository discountCodeTransactionRepository,
                           StoreRepository storeRepository) {
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.discountCodeTransactionRepository = discountCodeTransactionRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * Get store transaction summary for a store user
     * Includes today, last 7 days, and last 30 days subtotals
     */
    @Transactional(readOnly = true)
    public StoreTransactionSummary getStoreTransactionSummary(User storeUser) {
        if (storeUser.getStore() == null) {
            logger.warn("Store user {} has no associated store", storeUser.getUsername());
            return null;
        }

        Long storeId = storeUser.getStore().getId();
        String storeName = storeUser.getStore().getStore_name();
        
        logger.info("Getting transaction summary for store user {} (store: {})", storeUser.getUsername(), storeName);

        StoreTransactionSummary summary = new StoreTransactionSummary(storeId, storeName);

        // Calculate time boundaries
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime last7DaysStart = todayStart.minusDays(7);
        LocalDateTime last30DaysStart = todayStart.minusDays(30);

        // Get gift card transactions for the store
        List<GiftCardTransaction> giftCardTransactions = giftCardTransactionRepository.findByStoreId(storeId);
        
        // Get discount code transactions for the store
        List<DiscountCodeTransaction> discountCodeTransactions = discountCodeTransactionRepository.findByStoreId(storeId);

        // Calculate gift card transaction totals
        calculateGiftCardTotals(summary, giftCardTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        // Calculate discount code transaction totals
        calculateDiscountCodeTotals(summary, discountCodeTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        logger.info("Store transaction summary calculated for store {}: Today: {}, 7 days: {}, 30 days: {}", 
            storeName, summary.getTodayTotal(), summary.getLast7DaysTotal(), summary.getLast30DaysTotal());

        return summary;
    }

    /**
     * Get gift card transactions for a specific store
     */
    @Transactional(readOnly = true)
    public List<GiftCardTransaction> getGiftCardTransactionsForStore(User storeUser) {
        if (storeUser.getStore() == null) {
            logger.warn("Store user {} has no associated store", storeUser.getUsername());
            return List.of();
        }

        Long storeId = storeUser.getStore().getId();
        logger.info("Getting gift card transactions for store user {} (store: {})", storeUser.getUsername(), storeId);

        return giftCardTransactionRepository.findByStoreId(storeId);
    }

    /**
     * Get discount code transactions for a specific store
     */
    @Transactional(readOnly = true)
    public List<DiscountCodeTransaction> getDiscountCodeTransactionsForStore(User storeUser) {
        if (storeUser.getStore() == null) {
            logger.warn("Store user {} has no associated store", storeUser.getUsername());
            return List.of();
        }

        Long storeId = storeUser.getStore().getId();
        logger.info("Getting discount code transactions for store user {} (store: {})", storeUser.getUsername(), storeId);

        return discountCodeTransactionRepository.findByStoreId(storeId);
    }

    /**
     * Search gift card transactions for a specific store by gift card serial number
     */
    @Transactional(readOnly = true)
    public List<GiftCardTransaction> searchGiftCardTransactionsBySerial(User storeUser, String serialNumber) {
        if (storeUser.getStore() == null) {
            logger.warn("Store user {} has no associated store", storeUser.getUsername());
            return List.of();
        }

        Long storeId = storeUser.getStore().getId();
        logger.info("Searching gift card transactions by serial {} for store user {} (store: {})", 
            serialNumber, storeUser.getUsername(), storeId);

        return giftCardTransactionRepository.findByStoreIdAndGiftCardSerialNo(storeId, serialNumber);
    }

    /**
     * Search discount code transactions for a specific store by discount code
     */
    @Transactional(readOnly = true)
    public List<DiscountCodeTransaction> searchDiscountCodeTransactionsByCode(User storeUser, String discountCode) {
        if (storeUser.getStore() == null) {
            logger.warn("Store user {} has no associated store", storeUser.getUsername());
            return List.of();
        }

        Long storeId = storeUser.getStore().getId();
        logger.info("Searching discount code transactions by code {} for store user {} (store: {})", 
            discountCode, storeUser.getUsername(), storeId);

        return discountCodeTransactionRepository.findByStoreIdAndDiscountCode(storeId, discountCode);
    }

    /**
     * Validate if a user is a store user
     */
    public boolean isStoreUser(User user) {
        return user != null && 
               user.getRole() != null && 
               "STORE_USER".equals(user.getRole().getName()) &&
               user.getStore() != null;
    }

    /**
     * Get store information for a store user
     */
    @Transactional(readOnly = true)
    public Store getStoreForUser(User storeUser) {
        if (storeUser.getStore() == null) {
            return null;
        }

        Optional<Store> store = storeRepository.findById(storeUser.getStore().getId());
        return store.orElse(null);
    }

    private void calculateGiftCardTotals(StoreTransactionSummary summary, List<GiftCardTransaction> transactions,
                                       LocalDateTime todayStart, LocalDateTime todayEnd,
                                       LocalDateTime last7DaysStart, LocalDateTime last30DaysStart) {
        
        for (GiftCardTransaction transaction : transactions) {
            LocalDateTime trxDate = transaction.getTrxDate();
            BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

            // Only count debit transactions (actual spending)
            if (TransactionType.Debit.equals(transaction.getTransactionType())) {
                // Today
                if (trxDate.isAfter(todayStart) && trxDate.isBefore(todayEnd)) {
                    summary.setTodayTotal(summary.getTodayTotal().add(amount));
                    summary.setTodayTransactionCount(summary.getTodayTransactionCount() + 1);
                }

                // Last 7 days
                if (trxDate.isAfter(last7DaysStart) && trxDate.isBefore(todayEnd)) {
                    summary.setLast7DaysTotal(summary.getLast7DaysTotal().add(amount));
                    summary.setLast7DaysTransactionCount(summary.getLast7DaysTransactionCount() + 1);
                }

                // Last 30 days
                if (trxDate.isAfter(last30DaysStart) && trxDate.isBefore(todayEnd)) {
                    summary.setLast30DaysTotal(summary.getLast30DaysTotal().add(amount));
                    summary.setLast30DaysTransactionCount(summary.getLast30DaysTransactionCount() + 1);
                }
            }
        }
    }

    private void calculateDiscountCodeTotals(StoreTransactionSummary summary, List<DiscountCodeTransaction> transactions,
                                           LocalDateTime todayStart, LocalDateTime todayEnd,
                                           LocalDateTime last7DaysStart, LocalDateTime last30DaysStart) {
        
        for (DiscountCodeTransaction transaction : transactions) {
            LocalDateTime trxDate = transaction.getTrxDate();
            BigDecimal discountAmount = BigDecimal.valueOf(transaction.getDiscountAmount());

            // Only count redeem transactions (actual discount usage)
            if (TransactionType.Redeem.equals(transaction.getTrxType())) {
                // Today
                if (trxDate.isAfter(todayStart) && trxDate.isBefore(todayEnd)) {
                    summary.setTodayTotal(summary.getTodayTotal().add(discountAmount));
                    summary.setTodayTransactionCount(summary.getTodayTransactionCount() + 1);
                }

                // Last 7 days
                if (trxDate.isAfter(last7DaysStart) && trxDate.isBefore(todayEnd)) {
                    summary.setLast7DaysTotal(summary.getLast7DaysTotal().add(discountAmount));
                    summary.setLast7DaysTransactionCount(summary.getLast7DaysTransactionCount() + 1);
                }

                // Last 30 days
                if (trxDate.isAfter(last30DaysStart) && trxDate.isBefore(todayEnd)) {
                    summary.setLast30DaysTotal(summary.getLast30DaysTotal().add(discountAmount));
                    summary.setLast30DaysTransactionCount(summary.getLast30DaysTransactionCount() + 1);
                }
            }
        }
    }
}
