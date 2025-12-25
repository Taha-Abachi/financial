package com.pars.financial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.StoreTransactionSummary;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
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
     * Get transaction summary based on user role
     * - STORE_USER: Transactions for their assigned store
     * - COMPANY_USER: Transactions for all stores in their company
     * - SUPERADMIN: All transactions across all stores
     */
    @Transactional(readOnly = true)
    public StoreTransactionSummary getTransactionSummary(User user) {
        if (user == null || user.getRole() == null) {
            logger.warn("Invalid user or user role");
            return null;
        }

        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "STORE_USER":
                return getStoreTransactionSummary(user);
                
            case "COMPANY_USER":
                return getCompanyTransactionSummary(user);
                
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return getAllTransactionSummary(user);
                
            default:
                logger.warn("User {} with role {} is not authorized for transaction summary", 
                           user.getUsername(), roleName);
                return null;
        }
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

        // Get confirmed debit gift card transactions for the store
        List<GiftCardTransaction> giftCardTransactions = giftCardTransactionRepository.findConfirmedDebitTransactionsByStoreId(storeId);
        
        // Get confirmed redeem discount code transactions for the store
        List<DiscountCodeTransaction> discountCodeTransactions = discountCodeTransactionRepository.findConfirmedRedeemTransactionsByStoreId(storeId);

        // Calculate gift card transaction totals
        calculateGiftCardTotals(summary, giftCardTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        // Calculate discount code transaction totals
        calculateDiscountCodeTotals(summary, discountCodeTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        logger.info("Store transaction summary calculated for store {}: Today: {}, 7 days: {}, 30 days: {}", 
            storeName, summary.getTodayTotal(), summary.getLast7DaysTotal(), summary.getLast30DaysTotal());

        return summary;
    }

    /**
     * Get company transaction summary for a company user
     * Includes transactions from all stores in the company
     */
    @Transactional(readOnly = true)
    public StoreTransactionSummary getCompanyTransactionSummary(User companyUser) {
        if (companyUser.getCompany() == null) {
            logger.warn("Company user {} has no associated company", companyUser.getUsername());
            return null;
        }

        Long companyId = companyUser.getCompany().getId();
        String companyName = companyUser.getCompany().getName();
        
        logger.info("Getting transaction summary for company user {} (company: {})", 
                   companyUser.getUsername(), companyName);

        // Use companyId and companyName for the summary (storeId will be null, storeName will be company name)
        StoreTransactionSummary summary = new StoreTransactionSummary(null, "Company: " + companyName);

        // Calculate time boundaries
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime last7DaysStart = todayStart.minusDays(7);
        LocalDateTime last30DaysStart = todayStart.minusDays(30);

        // Get confirmed debit gift card transactions from all stores belonging to the company
        List<GiftCardTransaction> giftCardTransactions = giftCardTransactionRepository.findConfirmedDebitTransactionsByStoreCompanyId(companyId);
        
        // Get confirmed redeem discount code transactions from all stores belonging to the company
        List<DiscountCodeTransaction> discountCodeTransactions = 
            discountCodeTransactionRepository.findConfirmedRedeemTransactionsByStoreCompanyId(companyId);

        // Calculate gift card transaction totals
        calculateGiftCardTotals(summary, giftCardTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        // Calculate discount code transaction totals
        calculateDiscountCodeTotals(summary, discountCodeTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        logger.info("Company transaction summary calculated for company {}: Today: {}, 7 days: {}, 30 days: {}", 
            companyName, summary.getTodayTotal(), summary.getLast7DaysTotal(), summary.getLast30DaysTotal());

        return summary;
    }

    /**
     * Get all transactions summary for SUPERADMIN
     * Includes transactions from all stores across all companies
     */
    @Transactional(readOnly = true)
    public StoreTransactionSummary getAllTransactionSummary(User adminUser) {
        logger.info("Getting transaction summary for admin user {} (all transactions)", adminUser.getUsername());

        StoreTransactionSummary summary = new StoreTransactionSummary(null, "All Transactions");

        // Calculate time boundaries
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime last7DaysStart = todayStart.minusDays(7);
        LocalDateTime last30DaysStart = todayStart.minusDays(30);

        // Get all confirmed debit gift card transactions
        List<GiftCardTransaction> giftCardTransactions = 
            giftCardTransactionRepository.findAllConfirmedDebitTransactions();
        
        // Get all confirmed redeem discount code transactions
        List<DiscountCodeTransaction> discountCodeTransactions = 
            discountCodeTransactionRepository.findAllConfirmedRedeemTransactions();

        // Calculate gift card transaction totals
        calculateGiftCardTotals(summary, giftCardTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        // Calculate discount code transaction totals
        calculateDiscountCodeTotals(summary, discountCodeTransactions, todayStart, todayEnd, last7DaysStart, last30DaysStart);

        logger.info("All transactions summary calculated: Today: {}, 7 days: {}, 30 days: {}", 
            summary.getTodayTotal(), summary.getLast7DaysTotal(), summary.getLast30DaysTotal());

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

        return discountCodeTransactionRepository.findByStoreIdAndDiscountCodeCode(storeId, discountCode);
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

        Store store = storeRepository.findByIdWithRelationships(storeUser.getStore().getId());
        return store;
    }

    private void calculateGiftCardTotals(StoreTransactionSummary summary, List<GiftCardTransaction> transactions,
                                       LocalDateTime todayStart, LocalDateTime todayEnd,
                                       LocalDateTime last7DaysStart, LocalDateTime last30DaysStart) {
        
        // Transactions are already filtered to confirmed debit transactions at repository level
        for (GiftCardTransaction transaction : transactions) {
            LocalDateTime trxDate = transaction.getTrxDate();
            
            // Skip transactions with null date
            if (trxDate == null) {
                logger.warn("Gift card transaction {} has null trxDate, skipping", transaction.getId());
                continue;
            }
            
            BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

            // Today - include boundary values (>= todayStart and <= todayEnd)
            if (!trxDate.isBefore(todayStart) && !trxDate.isAfter(todayEnd)) {
                summary.setTodayTotal(summary.getTodayTotal().add(amount));
                summary.setTodayTransactionCount(summary.getTodayTransactionCount() + 1);
            }

            // Last 7 days - include boundary values (>= last7DaysStart and <= todayEnd)
            if (!trxDate.isBefore(last7DaysStart) && !trxDate.isAfter(todayEnd)) {
                summary.setLast7DaysTotal(summary.getLast7DaysTotal().add(amount));
                summary.setLast7DaysTransactionCount(summary.getLast7DaysTransactionCount() + 1);
            }

            // Last 30 days - include boundary values (>= last30DaysStart and <= todayEnd)
            if (!trxDate.isBefore(last30DaysStart) && !trxDate.isAfter(todayEnd)) {
                summary.setLast30DaysTotal(summary.getLast30DaysTotal().add(amount));
                summary.setLast30DaysTransactionCount(summary.getLast30DaysTransactionCount() + 1);
            }
        }
    }

    private void calculateDiscountCodeTotals(StoreTransactionSummary summary, List<DiscountCodeTransaction> transactions,
                                           LocalDateTime todayStart, LocalDateTime todayEnd,
                                           LocalDateTime last7DaysStart, LocalDateTime last30DaysStart) {
        
        // Transactions are already filtered to confirmed redeem transactions at repository level
        for (DiscountCodeTransaction transaction : transactions) {
            LocalDateTime trxDate = transaction.getTrxDate();
            
            // Skip transactions with null date
            if (trxDate == null) {
                logger.warn("Discount code transaction {} has null trxDate, skipping", transaction.getId());
                continue;
            }
            
            BigDecimal discountAmount = BigDecimal.valueOf(transaction.getDiscountAmount());

            // Today - include boundary values (>= todayStart and <= todayEnd)
            if (!trxDate.isBefore(todayStart) && !trxDate.isAfter(todayEnd)) {
                summary.setTodayTotal(summary.getTodayTotal().add(discountAmount));
                summary.setTodayTransactionCount(summary.getTodayTransactionCount() + 1);
            }

            // Last 7 days - include boundary values (>= last7DaysStart and <= todayEnd)
            if (!trxDate.isBefore(last7DaysStart) && !trxDate.isAfter(todayEnd)) {
                summary.setLast7DaysTotal(summary.getLast7DaysTotal().add(discountAmount));
                summary.setLast7DaysTransactionCount(summary.getLast7DaysTransactionCount() + 1);
            }

            // Last 30 days - include boundary values (>= last30DaysStart and <= todayEnd)
            if (!trxDate.isBefore(last30DaysStart) && !trxDate.isAfter(todayEnd)) {
                summary.setLast30DaysTotal(summary.getLast30DaysTotal().add(discountAmount));
                summary.setLast30DaysTransactionCount(summary.getLast30DaysTransactionCount() + 1);
            }
        }
    }
}
