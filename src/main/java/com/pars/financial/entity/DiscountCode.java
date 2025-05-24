package com.pars.financial.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
    @Index(name = "idx_discount_code", columnList = "code"),
    @Index(name = "idx_discount_serial", columnList = "serialNo")
})
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(20)")
    private String code;
    private Long serialNo;
    private int percentage;
    private long maxDiscountAmount = 0;
    private long minimumBillAmount = 0;
    private int usageLimit = 1;
    private int currentUsageCount = 0;

    private LocalDateTime issueDate;
    private LocalDate expiryDate;
    private LocalDateTime redeemDate;

    @Column(columnDefinition = "boolean default false")
    private boolean used = false;
    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(Long serialNo) {
        this.serialNo = serialNo;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public long getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(long maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public long getMinimumBillAmount() {
        return minimumBillAmount;
    }

    public void setMinimumBillAmount(long minimumBillAmount) {
        this.minimumBillAmount = minimumBillAmount;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getCurrentUsageCount() {
        return currentUsageCount;
    }

    public void setCurrentUsageCount(int currentUsageCount) {
        this.currentUsageCount = currentUsageCount;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getRedeemDate() {
        return redeemDate;
    }

    public void setRedeemDate(LocalDateTime redeemDate) {
        this.redeemDate = redeemDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
