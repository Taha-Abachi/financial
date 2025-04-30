package com.pars.financial.repository;

import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Integer> {
    GiftCardTransaction findByTransactionId(UUID transactionId);

    GiftCardTransaction findByTransactionTypeAndTransactionId(TransactionType transactionType, UUID transactionId);

    GiftCardTransaction findByClientTransactionId(String clientTransactionId);
    
    GiftCardTransaction findByTransactionTypeAndClientTransactionId(TransactionType transactionType, String clientTransactionId);

    List<GiftCardTransaction> findByGiftCardAndTransactionType(GiftCard giftCard, TransactionType transactionType);

    GiftCardTransaction findTopByTransactionIdOrderByTrxDateDesc(UUID transactionId);
}
