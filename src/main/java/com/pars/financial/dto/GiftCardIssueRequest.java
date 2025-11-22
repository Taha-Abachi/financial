package com.pars.financial.dto;

import com.pars.financial.enums.GiftCardType;

public class GiftCardIssueRequest {
    private long balance;
    private long realAmount;
    private long remainingValidityPeriod;
    private int count;
    private long companyId = 0;
    private boolean storeLimited = false;
    private java.util.List<Long> allowedStoreIds;
    private boolean itemCategoryLimited = false;
    private java.util.List<Long> allowedItemCategoryIds;
    private GiftCardType type = GiftCardType.PHYSICAL;

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getRealAmount() {return realAmount;}
    public void setRealAmount(long realAmount) {this.realAmount = realAmount;}

    public boolean isStoreLimited() {
        return storeLimited;
    }

    public void setStoreLimited(boolean storeLimited) {
        this.storeLimited = storeLimited;
    }

    public java.util.List<Long> getAllowedStoreIds() {
        return allowedStoreIds;
    }

    public void setAllowedStoreIds(java.util.List<Long> allowedStoreIds) {
        this.allowedStoreIds = allowedStoreIds;
    }

    public boolean isItemCategoryLimited() {
        return itemCategoryLimited;
    }

    public void setItemCategoryLimited(boolean itemCategoryLimited) {
        this.itemCategoryLimited = itemCategoryLimited;
    }

    public java.util.List<Long> getAllowedItemCategoryIds() {
        return allowedItemCategoryIds;
    }

    public void setAllowedItemCategoryIds(java.util.List<Long> allowedItemCategoryIds) {
        this.allowedItemCategoryIds = allowedItemCategoryIds;
    }

    public GiftCardType getType() {
        return type;
    }

    public void setType(GiftCardType type) {
        this.type = type;
    }
}