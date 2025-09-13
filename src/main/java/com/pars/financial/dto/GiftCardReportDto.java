package com.pars.financial.dto;

import java.time.LocalDateTime;

public class GiftCardReportDto {
    private Long totalCount;
    private Long totalBalance;
    private Long totalInitialAmount;
    private Long totalDebitTransactions;
    private Long totalDebitAmount;
    private Long activeCount;
    private Long blockedCount;
    private Long expiredCount;
    private Long usedCount;
    private Double averageBalance;
    private Double averageInitialAmount;
    private LocalDateTime reportGeneratedAt;

    // Constructors
    public GiftCardReportDto() {
        this.reportGeneratedAt = LocalDateTime.now();
    }

    public GiftCardReportDto(Long totalCount, Long totalBalance, Long totalInitialAmount, 
                           Long totalDebitTransactions, Long totalDebitAmount, 
                           Long activeCount, Long blockedCount, Long expiredCount, Long usedCount,
                           Double averageBalance, Double averageInitialAmount) {
        this.totalCount = totalCount;
        this.totalBalance = totalBalance;
        this.totalInitialAmount = totalInitialAmount;
        this.totalDebitTransactions = totalDebitTransactions;
        this.totalDebitAmount = totalDebitAmount;
        this.activeCount = activeCount;
        this.blockedCount = blockedCount;
        this.expiredCount = expiredCount;
        this.usedCount = usedCount;
        this.averageBalance = averageBalance;
        this.averageInitialAmount = averageInitialAmount;
        this.reportGeneratedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Long totalBalance) {
        this.totalBalance = totalBalance;
    }

    public Long getTotalInitialAmount() {
        return totalInitialAmount;
    }

    public void setTotalInitialAmount(Long totalInitialAmount) {
        this.totalInitialAmount = totalInitialAmount;
    }

    public Long getTotalDebitTransactions() {
        return totalDebitTransactions;
    }

    public void setTotalDebitTransactions(Long totalDebitTransactions) {
        this.totalDebitTransactions = totalDebitTransactions;
    }

    public Long getTotalDebitAmount() {
        return totalDebitAmount;
    }

    public void setTotalDebitAmount(Long totalDebitAmount) {
        this.totalDebitAmount = totalDebitAmount;
    }

    public Long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Long activeCount) {
        this.activeCount = activeCount;
    }

    public Long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(Long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public Long getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(Long expiredCount) {
        this.expiredCount = expiredCount;
    }

    public Long getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Long usedCount) {
        this.usedCount = usedCount;
    }

    public Double getAverageBalance() {
        return averageBalance;
    }

    public void setAverageBalance(Double averageBalance) {
        this.averageBalance = averageBalance;
    }

    public Double getAverageInitialAmount() {
        return averageInitialAmount;
    }

    public void setAverageInitialAmount(Double averageInitialAmount) {
        this.averageInitialAmount = averageInitialAmount;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    @Override
    public String toString() {
        return "GiftCardReportDto{" +
                "totalCount=" + totalCount +
                ", totalBalance=" + totalBalance +
                ", totalInitialAmount=" + totalInitialAmount +
                ", totalDebitTransactions=" + totalDebitTransactions +
                ", totalDebitAmount=" + totalDebitAmount +
                ", activeCount=" + activeCount +
                ", blockedCount=" + blockedCount +
                ", expiredCount=" + expiredCount +
                ", usedCount=" + usedCount +
                ", averageBalance=" + averageBalance +
                ", averageInitialAmount=" + averageInitialAmount +
                ", reportGeneratedAt=" + reportGeneratedAt +
                '}';
    }
}
