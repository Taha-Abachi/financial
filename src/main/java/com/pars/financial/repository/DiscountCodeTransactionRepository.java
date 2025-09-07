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
    Page<DiscountCodeTransaction> findAll(Pageable pageable);
}
