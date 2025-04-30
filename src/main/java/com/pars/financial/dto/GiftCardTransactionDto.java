package com.pars.financial.dto;

import com.pars.financial.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public class GiftCardTransactionDto {
    public String phoneNo;
    public UUID transactionId;
    public String giftCardSerialNo;
    public UUID requestId;
    public TransactionType trxType;
    public LocalDateTime trxDate = LocalDateTime.now();
    public Long storeId;
    public long amount;
    public String clientTransactionId;
    public String storeName;
}
