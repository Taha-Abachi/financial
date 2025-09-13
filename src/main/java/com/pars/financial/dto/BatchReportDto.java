package com.pars.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BatchReportDto {
    private Long batchId;
    private String batchNumber;
    private String description;
    private LocalDateTime requestDate;
    private LocalDateTime createdAt;
    private String batchType;
    private String status;
    
    // Overall batch statistics
    private Integer totalRequested;
    private Integer totalProcessed;
    private Integer totalFailed;
    private BigDecimal successRate;
    
    // Gift Card Statistics (if applicable)
    private GiftCardReportSummary giftCardSummary;
    
    // Discount Code Statistics (if applicable)
    private DiscountCodeReportSummary discountCodeSummary;
    
    // Overall financial summary
    private FinancialSummary financialSummary;

    public BatchReportDto() {}

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(Integer totalRequested) {
        this.totalRequested = totalRequested;
    }

    public Integer getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(Integer totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public Integer getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(Integer totalFailed) {
        this.totalFailed = totalFailed;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public GiftCardReportSummary getGiftCardSummary() {
        return giftCardSummary;
    }

    public void setGiftCardSummary(GiftCardReportSummary giftCardSummary) {
        this.giftCardSummary = giftCardSummary;
    }

    public DiscountCodeReportSummary getDiscountCodeSummary() {
        return discountCodeSummary;
    }

    public void setDiscountCodeSummary(DiscountCodeReportSummary discountCodeSummary) {
        this.discountCodeSummary = discountCodeSummary;
    }

    public FinancialSummary getFinancialSummary() {
        return financialSummary;
    }

    public void setFinancialSummary(FinancialSummary financialSummary) {
        this.financialSummary = financialSummary;
    }

    // Inner classes for detailed reporting
    public static class GiftCardReportSummary {
        private Integer totalGiftCards;
        private Integer activeGiftCards;
        private Integer usedGiftCards;
        private Integer expiredGiftCards;
        private BigDecimal totalInitialValue;
        private BigDecimal totalRemainingBalance;
        private BigDecimal totalUsedAmount;
        private BigDecimal averageInitialValue;
        private BigDecimal averageRemainingBalance;
        private BigDecimal utilizationRate;

        public GiftCardReportSummary() {}

        public Integer getTotalGiftCards() {
            return totalGiftCards;
        }

        public void setTotalGiftCards(Integer totalGiftCards) {
            this.totalGiftCards = totalGiftCards;
        }

        public Integer getActiveGiftCards() {
            return activeGiftCards;
        }

        public void setActiveGiftCards(Integer activeGiftCards) {
            this.activeGiftCards = activeGiftCards;
        }

        public Integer getUsedGiftCards() {
            return usedGiftCards;
        }

        public void setUsedGiftCards(Integer usedGiftCards) {
            this.usedGiftCards = usedGiftCards;
        }

        public Integer getExpiredGiftCards() {
            return expiredGiftCards;
        }

        public void setExpiredGiftCards(Integer expiredGiftCards) {
            this.expiredGiftCards = expiredGiftCards;
        }

        public BigDecimal getTotalInitialValue() {
            return totalInitialValue;
        }

        public void setTotalInitialValue(BigDecimal totalInitialValue) {
            this.totalInitialValue = totalInitialValue;
        }

        public BigDecimal getTotalRemainingBalance() {
            return totalRemainingBalance;
        }

        public void setTotalRemainingBalance(BigDecimal totalRemainingBalance) {
            this.totalRemainingBalance = totalRemainingBalance;
        }

        public BigDecimal getTotalUsedAmount() {
            return totalUsedAmount;
        }

        public void setTotalUsedAmount(BigDecimal totalUsedAmount) {
            this.totalUsedAmount = totalUsedAmount;
        }

        public BigDecimal getAverageInitialValue() {
            return averageInitialValue;
        }

        public void setAverageInitialValue(BigDecimal averageInitialValue) {
            this.averageInitialValue = averageInitialValue;
        }

        public BigDecimal getAverageRemainingBalance() {
            return averageRemainingBalance;
        }

        public void setAverageRemainingBalance(BigDecimal averageRemainingBalance) {
            this.averageRemainingBalance = averageRemainingBalance;
        }

        public BigDecimal getUtilizationRate() {
            return utilizationRate;
        }

        public void setUtilizationRate(BigDecimal utilizationRate) {
            this.utilizationRate = utilizationRate;
        }
    }

    public static class DiscountCodeReportSummary {
        private Integer totalDiscountCodes;
        private Integer activeDiscountCodes;
        private Integer usedDiscountCodes;
        private Integer expiredDiscountCodes;
        private Integer totalRedemptions;
        private BigDecimal totalDiscountGiven;
        private BigDecimal averageDiscountPerCode;
        private BigDecimal averageDiscountPerRedemption;
        private BigDecimal redemptionRate;

        public DiscountCodeReportSummary() {}

        public Integer getTotalDiscountCodes() {
            return totalDiscountCodes;
        }

        public void setTotalDiscountCodes(Integer totalDiscountCodes) {
            this.totalDiscountCodes = totalDiscountCodes;
        }

        public Integer getActiveDiscountCodes() {
            return activeDiscountCodes;
        }

        public void setActiveDiscountCodes(Integer activeDiscountCodes) {
            this.activeDiscountCodes = activeDiscountCodes;
        }

        public Integer getUsedDiscountCodes() {
            return usedDiscountCodes;
        }

        public void setUsedDiscountCodes(Integer usedDiscountCodes) {
            this.usedDiscountCodes = usedDiscountCodes;
        }

        public Integer getExpiredDiscountCodes() {
            return expiredDiscountCodes;
        }

        public void setExpiredDiscountCodes(Integer expiredDiscountCodes) {
            this.expiredDiscountCodes = expiredDiscountCodes;
        }

        public Integer getTotalRedemptions() {
            return totalRedemptions;
        }

        public void setTotalRedemptions(Integer totalRedemptions) {
            this.totalRedemptions = totalRedemptions;
        }

        public BigDecimal getTotalDiscountGiven() {
            return totalDiscountGiven;
        }

        public void setTotalDiscountGiven(BigDecimal totalDiscountGiven) {
            this.totalDiscountGiven = totalDiscountGiven;
        }

        public BigDecimal getAverageDiscountPerCode() {
            return averageDiscountPerCode;
        }

        public void setAverageDiscountPerCode(BigDecimal averageDiscountPerCode) {
            this.averageDiscountPerCode = averageDiscountPerCode;
        }

        public BigDecimal getAverageDiscountPerRedemption() {
            return averageDiscountPerRedemption;
        }

        public void setAverageDiscountPerRedemption(BigDecimal averageDiscountPerRedemption) {
            this.averageDiscountPerRedemption = averageDiscountPerRedemption;
        }

        public BigDecimal getRedemptionRate() {
            return redemptionRate;
        }

        public void setRedemptionRate(BigDecimal redemptionRate) {
            this.redemptionRate = redemptionRate;
        }
    }

    public static class FinancialSummary {
        private BigDecimal totalInitialValue;
        private BigDecimal totalRemainingValue;
        private BigDecimal totalUsedValue;
        private BigDecimal totalDiscountGiven;
        private BigDecimal overallUtilizationRate;
        private BigDecimal totalFinancialImpact;

        public FinancialSummary() {}

        public BigDecimal getTotalInitialValue() {
            return totalInitialValue;
        }

        public void setTotalInitialValue(BigDecimal totalInitialValue) {
            this.totalInitialValue = totalInitialValue;
        }

        public BigDecimal getTotalRemainingValue() {
            return totalRemainingValue;
        }

        public void setTotalRemainingValue(BigDecimal totalRemainingValue) {
            this.totalRemainingValue = totalRemainingValue;
        }

        public BigDecimal getTotalUsedValue() {
            return totalUsedValue;
        }

        public void setTotalUsedValue(BigDecimal totalUsedValue) {
            this.totalUsedValue = totalUsedValue;
        }

        public BigDecimal getTotalDiscountGiven() {
            return totalDiscountGiven;
        }

        public void setTotalDiscountGiven(BigDecimal totalDiscountGiven) {
            this.totalDiscountGiven = totalDiscountGiven;
        }

        public BigDecimal getOverallUtilizationRate() {
            return overallUtilizationRate;
        }

        public void setOverallUtilizationRate(BigDecimal overallUtilizationRate) {
            this.overallUtilizationRate = overallUtilizationRate;
        }

        public BigDecimal getTotalFinancialImpact() {
            return totalFinancialImpact;
        }

        public void setTotalFinancialImpact(BigDecimal totalFinancialImpact) {
            this.totalFinancialImpact = totalFinancialImpact;
        }
    }
}
