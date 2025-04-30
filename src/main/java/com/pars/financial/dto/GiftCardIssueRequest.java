package com.pars.financial.dto;


public class GiftCardIssueRequest {
    private long balance;
    private long realAmount;
    private long remainingValidityPeriod;
    private int count;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getRealAmount() {return realAmount;}
    public void setRealAmount(long realAmount) {this.realAmount = realAmount;}
}