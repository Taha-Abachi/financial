package com.pars.financial.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Integer> {
    GiftCardTransaction findByTransactionId(UUID transactionId);

    GiftCardTransaction findByTransactionTypeAndTransactionId(TransactionType transactionType, UUID transactionId);

    GiftCardTransaction findByClientTransactionId(String clientTransactionId);
    
    GiftCardTransaction findByTransactionTypeAndClientTransactionId(TransactionType transactionType, String clientTransactionId);

    List<GiftCardTransaction> findByGiftCardAndTransactionType(GiftCard giftCard, TransactionType transactionType);

    List<GiftCardTransaction> findByGiftCard(GiftCard giftCard);

    GiftCardTransaction findTopByGiftCardOrderByTrxDateDesc(GiftCard giftCard);

    GiftCardTransaction findTopByTransactionIdOrderByTrxDateDesc(UUID transactionId);
    
    // New methods for data cleansing
    List<GiftCardTransaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);
    
    List<GiftCardTransaction> findByTransactionType(TransactionType transactionType);
}
