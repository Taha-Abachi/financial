package com.pars.financial.dto;

import com.pars.financial.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public class DiscountCodeTransactionDto {
    public String phoneNo;
    public UUID transactionId;
    public long originalAmount;
    public long discountAmount;
    public int percentage;
    public long maxDiscountAmount;
    public String code;
    public TransactionType trxType;
    public LocalDateTime trxDate = LocalDateTime.now();
    public Long storeId;
    public String clientTransactionId;
    public String storeName;
}
