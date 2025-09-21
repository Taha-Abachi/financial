package com.pars.financial.dto;

import java.math.BigDecimal;

/**
 * DTO for store transaction summary with time-based subtotals
 */
public class StoreTransactionSummary {
    private Long storeId;
    private String storeName;
    private BigDecimal todayTotal;
    private BigDecimal last7DaysTotal;
    private BigDecimal last30DaysTotal;
    private int todayTransactionCount;
    private int last7DaysTransactionCount;
    private int last30DaysTransactionCount;

    public StoreTransactionSummary() {}

    public StoreTransactionSummary(Long storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.todayTotal = BigDecimal.ZERO;
        this.last7DaysTotal = BigDecimal.ZERO;
        this.last30DaysTotal = BigDecimal.ZERO;
        this.todayTransactionCount = 0;
        this.last7DaysTransactionCount = 0;
        this.last30DaysTransactionCount = 0;
    }

    // Getters and setters
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

    public BigDecimal getTodayTotal() {
        return todayTotal;
    }

    public void setTodayTotal(BigDecimal todayTotal) {
        this.todayTotal = todayTotal;
    }

    public BigDecimal getLast7DaysTotal() {
        return last7DaysTotal;
    }

    public void setLast7DaysTotal(BigDecimal last7DaysTotal) {
        this.last7DaysTotal = last7DaysTotal;
    }

    public BigDecimal getLast30DaysTotal() {
        return last30DaysTotal;
    }

    public void setLast30DaysTotal(BigDecimal last30DaysTotal) {
        this.last30DaysTotal = last30DaysTotal;
    }

    public int getTodayTransactionCount() {
        return todayTransactionCount;
    }

    public void setTodayTransactionCount(int todayTransactionCount) {
        this.todayTransactionCount = todayTransactionCount;
    }

    public int getLast7DaysTransactionCount() {
        return last7DaysTransactionCount;
    }

    public void setLast7DaysTransactionCount(int last7DaysTransactionCount) {
        this.last7DaysTransactionCount = last7DaysTransactionCount;
    }

    public int getLast30DaysTransactionCount() {
        return last30DaysTransactionCount;
    }

    public void setLast30DaysTransactionCount(int last30DaysTransactionCount) {
        this.last30DaysTransactionCount = last30DaysTransactionCount;
    }
}
