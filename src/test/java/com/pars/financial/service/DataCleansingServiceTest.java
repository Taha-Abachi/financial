package com.pars.financial.service;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.repository.GiftCardTransactionRepository;

@ExtendWith(MockitoExtension.class)
class DataCleansingServiceTest {

    @Mock
    private GiftCardTransactionRepository transactionRepository;
    
    @InjectMocks
    private DataCleansingService dataCleansingService;
    
    private UUID transactionId1;
    private UUID transactionId2;
    private UUID transactionId3;
    private GiftCardTransaction pendingDebit1;
    private GiftCardTransaction pendingDebit2;
    private GiftCardTransaction pendingDebit3;
    private GiftCardTransaction confirmation1;
    private GiftCardTransaction reversal2;
    private GiftCardTransaction refund3;
    private GiftCardTransaction orphanedConfirmation;
    
    @BeforeEach
    void setUp() {
        transactionId1 = UUID.randomUUID();
        transactionId2 = UUID.randomUUID();
        transactionId3 = UUID.randomUUID();
        
        // Setup pending debit transactions
        pendingDebit1 = new GiftCardTransaction();
        pendingDebit1.setTransactionId(transactionId1);
        pendingDebit1.setTransactionType(TransactionType.Debit);
        pendingDebit1.setStatus(TransactionStatus.Pending);
        
        pendingDebit2 = new GiftCardTransaction();
        pendingDebit2.setTransactionId(transactionId2);
        pendingDebit2.setTransactionType(TransactionType.Debit);
        pendingDebit2.setStatus(TransactionStatus.Pending);
        
        pendingDebit3 = new GiftCardTransaction();
        pendingDebit3.setTransactionId(transactionId3);
        pendingDebit3.setTransactionType(TransactionType.Debit);
        pendingDebit3.setStatus(TransactionStatus.Pending);
        
        // Setup settlement transactions
        confirmation1 = new GiftCardTransaction();
        confirmation1.setTransactionId(transactionId1);
        confirmation1.setTransactionType(TransactionType.Confirmation);
        
        reversal2 = new GiftCardTransaction();
        reversal2.setTransactionId(transactionId2);
        reversal2.setTransactionType(TransactionType.Reversal);
        
        refund3 = new GiftCardTransaction();
        refund3.setTransactionId(transactionId3);
        refund3.setTransactionType(TransactionType.Refund);
        
        // Setup orphaned transaction
        orphanedConfirmation = new GiftCardTransaction();
        orphanedConfirmation.setTransactionId(UUID.randomUUID());
        orphanedConfirmation.setTransactionType(TransactionType.Confirmation);
        orphanedConfirmation.setStatus(TransactionStatus.Pending);
    }
    
    @Test
    void testCleanseGiftCardTransactions_NoIssues() {
        // Given
        when(transactionRepository.findByTransactionTypeAndStatus(TransactionType.Debit, TransactionStatus.Pending))
            .thenReturn(Arrays.asList());
        
        when(transactionRepository.findByTransactionType(TransactionType.Confirmation))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Reversal))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Refund))
            .thenReturn(Arrays.asList());
        
        // When
        DataCleansingService.DataCleansingResult result = dataCleansingService.cleanseGiftCardTransactions();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getTotalFixed());
        
        verify(transactionRepository, never()).save(any());
    }
    
    @Test
    void testCleanseGiftCardTransactions_SimpleCase() {
        // Given - Just one pending debit with confirmation
        when(transactionRepository.findByTransactionTypeAndStatus(TransactionType.Debit, TransactionStatus.Pending))
            .thenReturn(Arrays.asList(pendingDebit1));
        
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, transactionId1))
            .thenReturn(confirmation1);
        
        // Mock refund check for the pending debit (no refund found)
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, transactionId1))
            .thenReturn(null);
        
        // Empty lists for other transaction types
        when(transactionRepository.findByTransactionType(TransactionType.Confirmation))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Reversal))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Refund))
            .thenReturn(Arrays.asList());
        
        // When
        DataCleansingService.DataCleansingResult result = dataCleansingService.cleanseGiftCardTransactions();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getRefundedTransactionsFixed());  // Refunds processed first (none found)
        assertEquals(1, result.getConfirmedTransactionsFixed()); // Then confirmations
        assertEquals(0, result.getReversedTransactionsFixed());
        assertEquals(0, result.getOrphanedTransactionsFixed());
        assertEquals(1, result.getTotalFixed());
        
        // Verify status updates for both transactions
        verify(transactionRepository).save(pendingDebit1);
        verify(transactionRepository).save(confirmation1);
        assertEquals(TransactionStatus.Confirmed, pendingDebit1.getStatus());
        assertEquals(TransactionStatus.Confirmed, confirmation1.getStatus());
    }
    
    @Test
    void testGenerateInconsistencyReport_NoIssues() {
        // Given
        when(transactionRepository.findByTransactionTypeAndStatus(TransactionType.Debit, TransactionStatus.Pending))
            .thenReturn(Arrays.asList());
        
        when(transactionRepository.findByTransactionType(TransactionType.Confirmation))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Reversal))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Refund))
            .thenReturn(Arrays.asList());
        
        // When
        DataCleansingService.DataInconsistencyReport report = dataCleansingService.generateInconsistencyReport();
        
        // Then
        assertNotNull(report);
        assertEquals(0, report.getTotalInconsistencies());
        assertFalse(report.hasInconsistencies());
    }
    
    @Test
    void testDataCleansingResult() {
        // Given
        DataCleansingService.DataCleansingResult result = new DataCleansingService.DataCleansingResult();
        
        // When
        result.setConfirmedTransactionsFixed(5);
        result.setReversedTransactionsFixed(3);
        result.setRefundedTransactionsFixed(2);
        result.setOrphanedTransactionsFixed(1);
        result.setExecutionTime(1000L);
        
        // Then
        assertEquals(5, result.getConfirmedTransactionsFixed());
        assertEquals(3, result.getReversedTransactionsFixed());
        assertEquals(2, result.getRefundedTransactionsFixed());
        assertEquals(1, result.getOrphanedTransactionsFixed());
        assertEquals(11, result.getTotalFixed());
        assertEquals(1000L, result.getExecutionTime());
        assertTrue(result.isSuccess());
    }
    
    @Test
    void testDataInconsistencyReport() {
        // Given
        DataCleansingService.DataInconsistencyReport report = new DataCleansingService.DataInconsistencyReport();
        
        // When
        report.setPendingWithConfirmations(3);
        report.setPendingWithRefunds(1);
        report.setPendingWithReversals(2);
        report.setOrphanedSettlements(4);
        
        // Then
        assertEquals(3, report.getPendingWithConfirmations());
        assertEquals(2, report.getPendingWithReversals());
        assertEquals(1, report.getPendingWithRefunds());
        assertEquals(4, report.getOrphanedSettlements());
        assertEquals(10, report.getTotalInconsistencies());
        assertTrue(report.hasInconsistencies());
    }
    
    @Test
    void testCleanseGiftCardTransactions_RefundPriority() {
        // Given - One pending debit with refund (should be processed first)
        when(transactionRepository.findByTransactionTypeAndStatus(TransactionType.Debit, TransactionStatus.Pending))
            .thenReturn(Arrays.asList(pendingDebit3)); // This has a refund
        
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, transactionId3))
            .thenReturn(refund3);
        
        // Mock confirmation check for the pending debit (no confirmation found)
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, transactionId3))
            .thenReturn(null);
        
        // Empty lists for other transaction types
        when(transactionRepository.findByTransactionType(TransactionType.Confirmation))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Reversal))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Refund))
            .thenReturn(Arrays.asList());
        
        // When
        DataCleansingService.DataCleansingResult result = dataCleansingService.cleanseGiftCardTransactions();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRefundedTransactionsFixed());  // Refund processed first
        assertEquals(0, result.getConfirmedTransactionsFixed());
        assertEquals(0, result.getReversedTransactionsFixed());
        assertEquals(0, result.getOrphanedTransactionsFixed());
        assertEquals(1, result.getTotalFixed());
        
        // Verify both transactions are updated and saved
        verify(transactionRepository).save(pendingDebit3);
        verify(transactionRepository).save(refund3);
        assertEquals(TransactionStatus.Refunded, pendingDebit3.getStatus());
        assertEquals(TransactionStatus.Refunded, refund3.getStatus());
    }
    
    @Test
    void testCleanseGiftCardTransactions_RefundWithConfirmation() {
        // Given - One pending debit with both refund and confirmation
        when(transactionRepository.findByTransactionTypeAndStatus(TransactionType.Debit, TransactionStatus.Pending))
            .thenReturn(Arrays.asList(pendingDebit1)); // This has both refund and confirmation
        
        // Create a refund transaction with the same transaction ID as pendingDebit1
        GiftCardTransaction refund1 = new GiftCardTransaction();
        refund1.setTransactionId(transactionId1);
        refund1.setTransactionType(TransactionType.Refund);
        refund1.setStatus(TransactionStatus.Pending);
        
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, transactionId1))
            .thenReturn(refund1);
        
        when(transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, transactionId1))
            .thenReturn(confirmation1);
        
        // Empty lists for other transaction types
        when(transactionRepository.findByTransactionType(TransactionType.Confirmation))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Reversal))
            .thenReturn(Arrays.asList());
        when(transactionRepository.findByTransactionType(TransactionType.Refund))
            .thenReturn(Arrays.asList());
        
        // When
        DataCleansingService.DataCleansingResult result = dataCleansingService.cleanseGiftCardTransactions();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRefundedTransactionsFixed());  // Refund processed first
        assertEquals(0, result.getConfirmedTransactionsFixed());
        assertEquals(0, result.getReversedTransactionsFixed());
        assertEquals(0, result.getOrphanedTransactionsFixed());
        assertEquals(1, result.getTotalFixed());
        
        // Verify all three transactions are updated and saved
        verify(transactionRepository).save(pendingDebit1);
        verify(transactionRepository).save(refund1);
        verify(transactionRepository).save(confirmation1);
        assertEquals(TransactionStatus.Refunded, pendingDebit1.getStatus());
        assertEquals(TransactionStatus.Refunded, refund1.getStatus());
        assertEquals(TransactionStatus.Refunded, confirmation1.getStatus());
    }
}
