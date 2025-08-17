package com.pars.financial.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.repository.GiftCardTransactionRepository;

@Service
public class DataCleansingService {

    private static final Logger logger = LoggerFactory.getLogger(DataCleansingService.class);

    private final GiftCardTransactionRepository transactionRepository;

    public DataCleansingService(GiftCardTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Cleanses gift card transaction data by fixing inconsistent statuses
     * This method should be run periodically to maintain data integrity
     * Processing order prioritizes refunds first (highest priority)
     * 
     * For each fix, both the debit transaction and its corresponding settlement transaction
     * (confirmation/reversal/refund) statuses are updated to maintain consistency.
     * 
     * @return DataCleansingResult containing statistics about the cleansing operation
     */
    @Transactional
    public DataCleansingResult cleanseGiftCardTransactions() {
        logger.info("Starting gift card transaction data cleansing...");
        
        DataCleansingResult result = new DataCleansingResult();
        
        try {
            // Fix pending debit transactions that have refunds FIRST (highest priority)
            int refundedCount = fixPendingDebitTransactionsWithRefunds();
            result.setRefundedTransactionsFixed(refundedCount);
            
            // Fix pending debit transactions that have confirmations
            int confirmedCount = fixPendingDebitTransactionsWithConfirmations();
            result.setConfirmedTransactionsFixed(confirmedCount);
            
            // Fix pending debit transactions that have reversals
            int reversedCount = fixPendingDebitTransactionsWithReversals();
            result.setReversedTransactionsFixed(reversedCount);
            
            // Fix orphaned confirmation/reversal/refund transactions
            int orphanedCount = fixOrphanedSettlementTransactions();
            result.setOrphanedTransactionsFixed(orphanedCount);
            
            logger.info("Gift card transaction data cleansing completed successfully. Fixed: {} refunded (priority), {} confirmed, {} reversed, {} orphaned", 
                refundedCount, confirmedCount, reversedCount, orphanedCount);
            
        } catch (Exception e) {
            logger.error("Error during gift card transaction data cleansing", e);
            result.setSuccess(false);
            result.setErrorMessage("Error during cleansing: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Fixes pending debit transactions that have confirmation transactions
     * Updates both debit and confirmation transaction statuses to Confirmed
     * Note: This method only processes transactions that haven't been processed by refund method
     */
    private int fixPendingDebitTransactionsWithConfirmations() {
        logger.debug("Fixing pending debit transactions with confirmations...");
        
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        int fixedCount = 0;
        for (GiftCardTransaction debit : pendingDebits) {
            // Skip if this debit has already been processed by refund method
            GiftCardTransaction refund = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Refund, debit.getTransactionId());
            if (refund != null) {
                logger.debug("Skipping debit {} as it has a refund transaction (already processed)", 
                    debit.getTransactionId());
                continue;
            }
            
            // Check if there's a confirmation transaction for this debit
            GiftCardTransaction confirmation = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Confirmation, debit.getTransactionId());
            
            if (confirmation != null) {
                logger.debug("Found pending debit {} with confirmation, updating both statuses to Confirmed", 
                    debit.getTransactionId());
                
                // Update debit transaction status
                debit.setStatus(TransactionStatus.Confirmed);
                transactionRepository.save(debit);
                
                // Update confirmation transaction status to Confirmed
                confirmation.setStatus(TransactionStatus.Confirmed);
                transactionRepository.save(confirmation);
                
                fixedCount++;
            }
        }
        
        logger.info("Fixed {} pending debit transactions with confirmations (updated both debit and confirmation statuses)", fixedCount);
        return fixedCount;
    }

    /**
     * Fixes pending debit transactions that have reversal transactions
     * Updates both debit and reversal transaction statuses to Reversed
     * Note: This method only processes transactions that haven't been processed by refund method
     */
    private int fixPendingDebitTransactionsWithReversals() {
        logger.debug("Fixing pending debit transactions with reversals...");
        
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        int fixedCount = 0;
        for (GiftCardTransaction debit : pendingDebits) {
            // Skip if this debit has already been processed by refund method
            GiftCardTransaction refund = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Refund, debit.getTransactionId());
            if (refund != null) {
                logger.debug("Skipping debit {} as it has a refund transaction (already processed)", 
                    debit.getTransactionId());
                continue;
            }
            
            // Check if there's a reversal transaction for this debit
            GiftCardTransaction reversal = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Reversal, debit.getTransactionId());
            
            if (reversal != null) {
                logger.debug("Found pending debit {} with reversal, updating both statuses to Reversed", 
                    debit.getTransactionId());
                
                // Update debit transaction status
                debit.setStatus(TransactionStatus.Reversed);
                transactionRepository.save(debit);
                
                // Update reversal transaction status to Reversed
                reversal.setStatus(TransactionStatus.Reversed);
                transactionRepository.save(reversal);
                
                fixedCount++;
            }
        }
        
        logger.info("Fixed {} pending debit transactions with reversals (updated both debit and reversal statuses)", fixedCount);
        return fixedCount;
    }

    /**
     * Fixes pending debit transactions that have refund transactions
     * Updates debit, refund, and any corresponding confirmation transaction statuses to Refunded
     */
    private int fixPendingDebitTransactionsWithRefunds() {
        logger.debug("Fixing pending debit transactions with refunds...");
        
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        int fixedCount = 0;
        for (GiftCardTransaction debit : pendingDebits) {
            // Check if there's a refund transaction for this debit
            GiftCardTransaction refund = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Refund, debit.getTransactionId());
            
            if (refund != null) {
                logger.debug("Found pending debit {} with refund, updating debit, refund, and confirmation statuses to Refunded", 
                    debit.getTransactionId());
                
                // Update debit transaction status
                debit.setStatus(TransactionStatus.Refunded);
                transactionRepository.save(debit);
                
                // Update refund transaction status to Refunded
                refund.setStatus(TransactionStatus.Refunded);
                transactionRepository.save(refund);
                
                // Check if there's a confirmation transaction for this debit and update it to Refunded
                GiftCardTransaction confirmation = transactionRepository.findByTransactionTypeAndTransactionId(
                    TransactionType.Confirmation, debit.getTransactionId());
                if (confirmation != null) {
                    logger.debug("Found confirmation transaction {} for refunded debit, updating status to Refunded", 
                        confirmation.getTransactionId());
                    confirmation.setStatus(TransactionStatus.Refunded);
                    transactionRepository.save(confirmation);
                }
                
                fixedCount++;
            }
        }
        
        logger.info("Fixed {} pending debit transactions with refunds (updated debit, refund, and confirmation statuses)", fixedCount);
        return fixedCount;
    }

    /**
     * Fixes orphaned settlement transactions (confirmation/reversal/refund) 
     * that don't have corresponding debit transactions
     */
    private int fixOrphanedSettlementTransactions() {
        logger.debug("Fixing orphaned settlement transactions...");
        
        int fixedCount = 0;
        
        // Check for orphaned confirmations
        List<GiftCardTransaction> confirmations = transactionRepository.findByTransactionType(TransactionType.Confirmation);
        for (GiftCardTransaction confirmation : confirmations) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, confirmation.getTransactionId());
            
            if (debit == null) {
                logger.warn("Found orphaned confirmation transaction {} without debit transaction, marking as invalid", 
                    confirmation.getTransactionId());
                
                confirmation.setStatus(TransactionStatus.Unkown);
                transactionRepository.save(confirmation);
                fixedCount++;
            }
        }
        
        // Check for orphaned reversals
        List<GiftCardTransaction> reversals = transactionRepository.findByTransactionType(TransactionType.Reversal);
        for (GiftCardTransaction reversal : reversals) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, reversal.getTransactionId());
            
            if (debit == null) {
                logger.warn("Found orphaned reversal transaction {} without debit transaction, marking as invalid", 
                    reversal.getTransactionId());
                
                reversal.setStatus(TransactionStatus.Unkown);
                transactionRepository.save(reversal);
                fixedCount++;
            }
        }
        
        // Check for orphaned refunds
        List<GiftCardTransaction> refunds = transactionRepository.findByTransactionType(TransactionType.Refund);
        for (GiftCardTransaction refund : refunds) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, refund.getTransactionId());
            
            if (debit == null) {
                logger.warn("Found orphaned refund transaction {} without debit transaction, marking as invalid", 
                    refund.getTransactionId());
                
                refund.setStatus(TransactionStatus.Unkown);
                transactionRepository.save(refund);
                fixedCount++;
            }
        }
        
        logger.info("Fixed {} orphaned settlement transactions", fixedCount);
        return fixedCount;
    }

    /**
     * Generates a report of data inconsistencies found in gift card transactions
     * This method doesn't fix anything, just reports issues
     */
    public DataInconsistencyReport generateInconsistencyReport() {
        logger.info("Generating data inconsistency report for gift card transactions...");
        
        DataInconsistencyReport report = new DataInconsistencyReport();
        
        // Count pending debit transactions with confirmations
        int pendingWithConfirmations = countPendingDebitsWithConfirmations();
        report.setPendingWithConfirmations(pendingWithConfirmations);
        
        // Count pending debit transactions with reversals
        int pendingWithReversals = countPendingDebitsWithReversals();
        report.setPendingWithReversals(pendingWithReversals);
        
        // Count pending debit transactions with refunds
        int pendingWithRefunds = countPendingDebitsWithRefunds();
        report.setPendingWithRefunds(pendingWithRefunds);
        
        // Count orphaned settlement transactions
        int orphanedSettlements = countOrphanedSettlementTransactions();
        report.setOrphanedSettlements(orphanedSettlements);
        
        logger.info("Inconsistency report generated: {} pending with confirmations, {} with reversals, {} with refunds, {} orphaned", 
            pendingWithConfirmations, pendingWithReversals, pendingWithRefunds, orphanedSettlements);
        
        return report;
    }

    private int countPendingDebitsWithConfirmations() {
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        return (int) pendingDebits.stream()
            .filter(debit -> {
                GiftCardTransaction confirmation = transactionRepository.findByTransactionTypeAndTransactionId(
                    TransactionType.Confirmation, debit.getTransactionId());
                return confirmation != null;
            })
            .count();
    }

    private int countPendingDebitsWithReversals() {
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        return (int) pendingDebits.stream()
            .filter(debit -> {
                GiftCardTransaction reversal = transactionRepository.findByTransactionTypeAndTransactionId(
                    TransactionType.Reversal, debit.getTransactionId());
                return reversal != null;
            })
            .count();
    }

    private int countPendingDebitsWithRefunds() {
        List<GiftCardTransaction> pendingDebits = transactionRepository.findByTransactionTypeAndStatus(
            TransactionType.Debit, TransactionStatus.Pending);
        
        return (int) pendingDebits.stream()
            .filter(debit -> {
                GiftCardTransaction refund = transactionRepository.findByTransactionTypeAndTransactionId(
                    TransactionType.Refund, debit.getTransactionId());
                return refund != null;
            })
            .count();
    }

    private int countOrphanedSettlementTransactions() {
        int orphanedCount = 0;
        
        // Count orphaned confirmations
        List<GiftCardTransaction> confirmations = transactionRepository.findByTransactionType(TransactionType.Confirmation);
        for (GiftCardTransaction confirmation : confirmations) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, confirmation.getTransactionId());
            if (debit == null) {
                orphanedCount++;
            }
        }
        
        // Count orphaned reversals
        List<GiftCardTransaction> reversals = transactionRepository.findByTransactionType(TransactionType.Reversal);
        for (GiftCardTransaction reversal : reversals) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, reversal.getTransactionId());
            if (debit == null) {
                orphanedCount++;
            }
        }
        
        // Count orphaned refunds
        List<GiftCardTransaction> refunds = transactionRepository.findByTransactionType(TransactionType.Refund);
        for (GiftCardTransaction refund : refunds) {
            GiftCardTransaction debit = transactionRepository.findByTransactionTypeAndTransactionId(
                TransactionType.Debit, refund.getTransactionId());
            if (debit == null) {
                orphanedCount++;
            }
        }
        
        return orphanedCount;
    }

    /**
     * Result class for data cleansing operations
     */
    public static class DataCleansingResult {
        private boolean success = true;
        private String errorMessage;
        private int confirmedTransactionsFixed;
        private int reversedTransactionsFixed;
        private int refundedTransactionsFixed;
        private int orphanedTransactionsFixed;
        private long executionTime;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public int getConfirmedTransactionsFixed() { return confirmedTransactionsFixed; }
        public void setConfirmedTransactionsFixed(int confirmedTransactionsFixed) { this.confirmedTransactionsFixed = confirmedTransactionsFixed; }
        
        public int getReversedTransactionsFixed() { return reversedTransactionsFixed; }
        public void setReversedTransactionsFixed(int reversedTransactionsFixed) { this.reversedTransactionsFixed = reversedTransactionsFixed; }
        
        public int getRefundedTransactionsFixed() { return refundedTransactionsFixed; }
        public void setRefundedTransactionsFixed(int refundedTransactionsFixed) { this.refundedTransactionsFixed = refundedTransactionsFixed; }
        
        public int getOrphanedTransactionsFixed() { return orphanedTransactionsFixed; }
        public void setOrphanedTransactionsFixed(int orphanedTransactionsFixed) { this.orphanedTransactionsFixed = orphanedTransactionsFixed; }
        
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        
        public int getTotalFixed() {
            return confirmedTransactionsFixed + reversedTransactionsFixed + refundedTransactionsFixed + orphanedTransactionsFixed;
        }
    }

    /**
     * Report class for data inconsistencies
     */
    public static class DataInconsistencyReport {
        private int pendingWithConfirmations;
        private int pendingWithReversals;
        private int pendingWithRefunds;
        private int orphanedSettlements;

        // Getters and setters
        public int getPendingWithConfirmations() { return pendingWithConfirmations; }
        public void setPendingWithConfirmations(int pendingWithConfirmations) { this.pendingWithConfirmations = pendingWithConfirmations; }
        
        public int getPendingWithReversals() { return pendingWithReversals; }
        public void setPendingWithReversals(int pendingWithReversals) { this.pendingWithReversals = pendingWithReversals; }
        
        public int getPendingWithRefunds() { return pendingWithRefunds; }
        public void setPendingWithRefunds(int pendingWithRefunds) { this.pendingWithRefunds = pendingWithRefunds; }
        
        public int getOrphanedSettlements() { return orphanedSettlements; }
        public void setOrphanedSettlements(int orphanedSettlements) { this.orphanedSettlements = orphanedSettlements; }
        
        public int getTotalInconsistencies() {
            return pendingWithConfirmations + pendingWithReversals + pendingWithRefunds + orphanedSettlements;
        }
        
        public boolean hasInconsistencies() {
            return getTotalInconsistencies() > 0;
        }
    }
}
