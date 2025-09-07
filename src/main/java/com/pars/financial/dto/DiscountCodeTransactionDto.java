package com.pars.financial.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pars.financial.enums.DiscountType;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

public class DiscountCodeTransactionDto {
    public String phoneNo;
    public UUID transactionId;
    public long originalAmount;
    public long discountAmount;
    public int percentage;
    public long maxDiscountAmount;
    public String code;
    public DiscountType discountType;
    public TransactionType trxType;
    public TransactionStatus status;
    public LocalDateTime trxDate = LocalDateTime.now();
    public Long storeId;
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
