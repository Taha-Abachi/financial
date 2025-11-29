package com.pars.financial.dto;

import java.math.BigDecimal;

/**
 * Store-level settlement summary
 */
public class StoreSettlementDto {
    private Long storeId;
    private String storeName;
    
    // Payable: Money owed to this store (from gift cards issued by other companies)
    private BigDecimal payable;
    
    // Receivable: Money to be received by this store (from gift cards issued by this store's company used elsewhere)
    private BigDecimal receivable;
    
    // Net amount (receivable - payable)
    private BigDecimal netAmount;
    
    // Transaction count
    private Long transactionCount;

    public StoreSettlementDto() {
        this.payable = BigDecimal.ZERO;
        this.receivable = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
        this.transactionCount = 0L;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public BigDecimal getPayable() {
        return payable;
    }

    public void setPayable(BigDecimal payable) {
        this.payable = payable;
    }

    public BigDecimal getReceivable() {
        return receivable;
    }

    public void setReceivable(BigDecimal receivable) {
        this.receivable = receivable;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}

