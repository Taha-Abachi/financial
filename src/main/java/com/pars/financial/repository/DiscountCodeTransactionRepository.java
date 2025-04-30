package com.pars.financial.repository;

import com.pars.financial.entity.DiscountCode;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiscountCodeTransactionRepository extends JpaRepository<DiscountCodeTransaction, Long> {
    
    DiscountCodeTransaction findByTransactionId(UUID transactionId);

    DiscountCodeTransaction findByDiscountCodeAndTrxType(DiscountCode discountCode, TransactionType trxType);

    DiscountCodeTransaction findByClientTransactionIdAndTrxType(String clientTransactionId, TransactionType trxType);

    DiscountCodeTransaction findByTransactionIdAndTrxType(UUID transactionId, TransactionType trxType);
}
