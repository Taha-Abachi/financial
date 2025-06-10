package com.pars.financial.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

public class GiftCardTransactionDto {
    public String phoneNo;
    public UUID transactionId;
    public String giftCardSerialNo;
    public UUID requestId;
    public TransactionType trxType;
    public TransactionStatus status;
    public LocalDateTime trxDate = LocalDateTime.now();
    public Long storeId;
    public long amount;
    public String clientTransactionId;
    public String storeName;
    public String orderno;
    public String description;

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
