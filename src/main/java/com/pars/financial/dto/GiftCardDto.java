package com.pars.financial.dto;


import java.time.LocalDate;
import java.util.List;

public class GiftCardDto {
    public String serialNo;
    public long balance;
    public long realAmount;
    public long remainingValidityPeriod;
    public LocalDate expiryDate;
    public boolean isActive;
    public boolean isBlocked;
    public boolean isAnonymous;
    public Long identifier;

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
}
