package com.pars.financial.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.pars.financial.enums.GiftCardType;

public class GiftCardDto {
    public String serialNo;
    public long balance;
    public long realAmount;
    public long initialAmount;
    public long remainingValidityPeriod;
    public LocalDate expiryDate;
    public boolean isActive;
    public boolean isBlocked;
    public boolean isAnonymous;
    public Long identifier;
    public Long companyId;
    public boolean storeLimited;
    public boolean itemCategoryLimited;
    public java.util.List<StoreDto> allowedStores;
    public java.util.List<ItemCategoryDto> allowedItemCategories;
    public Long batchId;
    public String batchNumber;
    public GiftCardType type;
    public Long blockedByUserId;
    public String blockedByUsername;
    public LocalDateTime blockedDate;

    public List<GiftCardTransactionDto> transactions;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(long initialAmount) {
        this.initialAmount = initialAmount;
    }

    public long getRemainingValidityPeriod() {
        return remainingValidityPeriod;
    }

    public void setRemainingValidityPeriod(long remainingValidityPeriod) {
        this.remainingValidityPeriod = remainingValidityPeriod;
    }

    public List<GiftCardTransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<GiftCardTransactionDto> transactions) {
        this.transactions = transactions;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public GiftCardType getType() {
        return type;
    }

    public void setType(GiftCardType type) {
        this.type = type;
    }
}
