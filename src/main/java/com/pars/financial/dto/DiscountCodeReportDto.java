package com.pars.financial.dto;

import java.time.LocalDateTime;

public class DiscountCodeReportDto {
    private Long totalCount;
    private Long totalUsedCount;
    private Long totalUnusedCount;
    private Long totalActiveCount;
    private Long totalInactiveCount;
    private Long totalExpiredCount;
    private Long totalRedeemTransactions;
    private Long totalDiscountAmount;
    private Long totalOriginalAmount;
    private Double averagePercentage;
    private Double averageMaxDiscountAmount;
    private Double averageMinimumBillAmount;
    private Double averageUsageCount;
    private Double averageUsageLimit;
    private LocalDateTime reportGeneratedAt;

    // Constructors
    public DiscountCodeReportDto() {
        this.reportGeneratedAt = LocalDateTime.now();
    }

    public DiscountCodeReportDto(Long totalCount, Long totalUsedCount, Long totalUnusedCount,
                               Long totalActiveCount, Long totalInactiveCount, Long totalExpiredCount,
                               Long totalRedeemTransactions, Long totalDiscountAmount, Long totalOriginalAmount,
                               Double averagePercentage, Double averageMaxDiscountAmount, Double averageMinimumBillAmount,
                               Double averageUsageCount, Double averageUsageLimit) {
        this.totalCount = totalCount;
        this.totalUsedCount = totalUsedCount;
        this.totalUnusedCount = totalUnusedCount;
        this.totalActiveCount = totalActiveCount;
        this.totalInactiveCount = totalInactiveCount;
        this.totalExpiredCount = totalExpiredCount;
        this.totalRedeemTransactions = totalRedeemTransactions;
        this.totalDiscountAmount = totalDiscountAmount;
        this.totalOriginalAmount = totalOriginalAmount;
        this.averagePercentage = averagePercentage;
        this.averageMaxDiscountAmount = averageMaxDiscountAmount;
        this.averageMinimumBillAmount = averageMinimumBillAmount;
        this.averageUsageCount = averageUsageCount;
        this.averageUsageLimit = averageUsageLimit;
        this.reportGeneratedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalUsedCount() {
        return totalUsedCount;
    }

    public void setTotalUsedCount(Long totalUsedCount) {
        this.totalUsedCount = totalUsedCount;
    }

    public Long getTotalUnusedCount() {
        return totalUnusedCount;
    }

    public void setTotalUnusedCount(Long totalUnusedCount) {
        this.totalUnusedCount = totalUnusedCount;
    }

    public Long getTotalActiveCount() {
        return totalActiveCount;
    }

    public void setTotalActiveCount(Long totalActiveCount) {
        this.totalActiveCount = totalActiveCount;
    }

    public Long getTotalInactiveCount() {
        return totalInactiveCount;
    }

    public void setTotalInactiveCount(Long totalInactiveCount) {
        this.totalInactiveCount = totalInactiveCount;
    }

    public Long getTotalExpiredCount() {
        return totalExpiredCount;
    }

    public void setTotalExpiredCount(Long totalExpiredCount) {
        this.totalExpiredCount = totalExpiredCount;
    }

    public Long getTotalRedeemTransactions() {
        return totalRedeemTransactions;
    }

    public void setTotalRedeemTransactions(Long totalRedeemTransactions) {
        this.totalRedeemTransactions = totalRedeemTransactions;
    }

    public Long getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(Long totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public Long getTotalOriginalAmount() {
        return totalOriginalAmount;
    }

    public void setTotalOriginalAmount(Long totalOriginalAmount) {
        this.totalOriginalAmount = totalOriginalAmount;
    }

    public Double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(Double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public Double getAverageMaxDiscountAmount() {
        return averageMaxDiscountAmount;
    }

    public void setAverageMaxDiscountAmount(Double averageMaxDiscountAmount) {
        this.averageMaxDiscountAmount = averageMaxDiscountAmount;
    }

    public Double getAverageMinimumBillAmount() {
        return averageMinimumBillAmount;
    }

    public void setAverageMinimumBillAmount(Double averageMinimumBillAmount) {
        this.averageMinimumBillAmount = averageMinimumBillAmount;
    }

    public Double getAverageUsageCount() {
        return averageUsageCount;
    }

    public void setAverageUsageCount(Double averageUsageCount) {
        this.averageUsageCount = averageUsageCount;
    }

    public Double getAverageUsageLimit() {
        return averageUsageLimit;
    }

    public void setAverageUsageLimit(Double averageUsageLimit) {
        this.averageUsageLimit = averageUsageLimit;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    @Override
    public String toString() {
        return "DiscountCodeReportDto{" +
                "totalCount=" + totalCount +
                ", totalUsedCount=" + totalUsedCount +
                ", totalUnusedCount=" + totalUnusedCount +
                ", totalActiveCount=" + totalActiveCount +
                ", totalInactiveCount=" + totalInactiveCount +
                ", totalExpiredCount=" + totalExpiredCount +
                ", totalRedeemTransactions=" + totalRedeemTransactions +
                ", totalDiscountAmount=" + totalDiscountAmount +
                ", totalOriginalAmount=" + totalOriginalAmount +
                ", averagePercentage=" + averagePercentage +
                ", averageMaxDiscountAmount=" + averageMaxDiscountAmount +
                ", averageMinimumBillAmount=" + averageMinimumBillAmount +
                ", averageUsageCount=" + averageUsageCount +
                ", averageUsageLimit=" + averageUsageLimit +
                ", reportGeneratedAt=" + reportGeneratedAt +
                '}';
    }
}
