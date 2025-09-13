package com.pars.financial.service;

import com.pars.financial.dto.BatchReportDto;
import com.pars.financial.entity.Batch;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.DiscountCode;
import com.pars.financial.repository.BatchRepository;
import com.pars.financial.repository.GiftCardRepository;
import com.pars.financial.repository.DiscountCodeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BatchReportService {
    private static final Logger logger = LoggerFactory.getLogger(BatchReportService.class);

    private final BatchRepository batchRepository;
    private final GiftCardRepository giftCardRepository;
    private final DiscountCodeRepository discountCodeRepository;
    public BatchReportService(BatchRepository batchRepository, 
                             GiftCardRepository giftCardRepository,
                             DiscountCodeRepository discountCodeRepository) {
        this.batchRepository = batchRepository;
        this.giftCardRepository = giftCardRepository;
        this.discountCodeRepository = discountCodeRepository;
    }

    public BatchReportDto generateBatchReport(Long batchId) {
        logger.info("Generating report for batch: {}", batchId);
        
        Optional<Batch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            logger.warn("Batch not found with id: {}", batchId);
            return null;
        }

        Batch batch = batchOpt.get();
        BatchReportDto report = new BatchReportDto();
        
        // Set basic batch information
        report.setBatchId(batch.getId());
        report.setBatchNumber(batch.getBatchNumber());
        report.setDescription(batch.getDescription());
        report.setRequestDate(batch.getRequestDate());
        report.setCreatedAt(batch.getCreatedAt());
        report.setBatchType(batch.getBatchType().toString());
        report.setStatus(batch.getStatus().toString());
        
        // Set overall statistics
        report.setTotalRequested(batch.getTotalCount());
        report.setTotalProcessed(batch.getProcessedCount());
        report.setTotalFailed(batch.getFailedCount());
        
        // Calculate success rate
        if (batch.getTotalCount() > 0) {
            BigDecimal successRate = BigDecimal.valueOf(batch.getProcessedCount())
                    .divide(BigDecimal.valueOf(batch.getTotalCount()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            report.setSuccessRate(successRate);
        } else {
            report.setSuccessRate(BigDecimal.ZERO);
        }

        // Generate type-specific reports
        if (batch.getBatchType() == Batch.BatchType.GIFT_CARD) {
            report.setGiftCardSummary(generateGiftCardReport(batch));
        } else if (batch.getBatchType() == Batch.BatchType.DISCOUNT_CODE) {
            report.setDiscountCodeSummary(generateDiscountCodeReport(batch));
        }

        // Generate overall financial summary
        report.setFinancialSummary(generateFinancialSummary(batch));

        logger.info("Generated report for batch: {} - Type: {}, Processed: {}/{}", 
                   batch.getBatchNumber(), batch.getBatchType(), 
                   batch.getProcessedCount(), batch.getTotalCount());
        
        return report;
    }

    private BatchReportDto.GiftCardReportSummary generateGiftCardReport(Batch batch) {
        logger.debug("Generating gift card report for batch: {}", batch.getBatchNumber());
        
        // Get all gift cards for this batch
        List<GiftCard> giftCards = giftCardRepository.findByBatchId(batch.getId());
        
        BatchReportDto.GiftCardReportSummary summary = new BatchReportDto.GiftCardReportSummary();
        
        if (giftCards.isEmpty()) {
            logger.warn("No gift cards found for batch: {}", batch.getBatchNumber());
            return summary;
        }

        // Basic counts
        summary.setTotalGiftCards(giftCards.size());
        
        int activeCount = 0;
        int usedCount = 0;
        int expiredCount = 0;
        
        BigDecimal totalInitialValue = BigDecimal.ZERO;
        BigDecimal totalRemainingBalance = BigDecimal.ZERO;
        BigDecimal totalUsedAmount = BigDecimal.ZERO;
        
        LocalDate today = LocalDate.now();
        
        for (GiftCard giftCard : giftCards) {
            // Count by status
            if (giftCard.isActive() && !giftCard.isBlocked()) {
                if (giftCard.getExpiryDate() != null && giftCard.getExpiryDate().isBefore(today)) {
                    expiredCount++;
                } else {
                    activeCount++;
                }
            }
            
            if (giftCard.getBalance() < giftCard.getInitialAmount()) {
                usedCount++;
            }
            
            // Calculate values
            BigDecimal initialValue = BigDecimal.valueOf(giftCard.getInitialAmount());
            BigDecimal remainingBalance = BigDecimal.valueOf(giftCard.getBalance());
            BigDecimal usedAmount = initialValue.subtract(remainingBalance);
            
            totalInitialValue = totalInitialValue.add(initialValue);
            totalRemainingBalance = totalRemainingBalance.add(remainingBalance);
            totalUsedAmount = totalUsedAmount.add(usedAmount);
        }
        
        summary.setActiveGiftCards(activeCount);
        summary.setUsedGiftCards(usedCount);
        summary.setExpiredGiftCards(expiredCount);
        
        summary.setTotalInitialValue(totalInitialValue);
        summary.setTotalRemainingBalance(totalRemainingBalance);
        summary.setTotalUsedAmount(totalUsedAmount);
        
        // Calculate averages
        if (giftCards.size() > 0) {
            summary.setAverageInitialValue(totalInitialValue.divide(BigDecimal.valueOf(giftCards.size()), 2, RoundingMode.HALF_UP));
            summary.setAverageRemainingBalance(totalRemainingBalance.divide(BigDecimal.valueOf(giftCards.size()), 2, RoundingMode.HALF_UP));
        }
        
        // Calculate utilization rate
        if (totalInitialValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = totalUsedAmount.divide(totalInitialValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            summary.setUtilizationRate(utilizationRate);
        } else {
            summary.setUtilizationRate(BigDecimal.ZERO);
        }
        
        logger.debug("Gift card report completed - Total: {}, Active: {}, Used: {}, Expired: {}", 
                    giftCards.size(), activeCount, usedCount, expiredCount);
        
        return summary;
    }

    private BatchReportDto.DiscountCodeReportSummary generateDiscountCodeReport(Batch batch) {
        logger.debug("Generating discount code report for batch: {}", batch.getBatchNumber());
        
        // Get all discount codes for this batch
        List<DiscountCode> discountCodes = discountCodeRepository.findByBatchId(batch.getId());
        
        BatchReportDto.DiscountCodeReportSummary summary = new BatchReportDto.DiscountCodeReportSummary();
        
        if (discountCodes.isEmpty()) {
            logger.warn("No discount codes found for batch: {}", batch.getBatchNumber());
            return summary;
        }

        // Basic counts
        summary.setTotalDiscountCodes(discountCodes.size());
        
        int activeCount = 0;
        int usedCount = 0;
        int expiredCount = 0;
        int totalRedemptions = 0;
        
        BigDecimal totalDiscountGiven = BigDecimal.ZERO;
        
        LocalDate today = LocalDate.now();
        
        for (DiscountCode discountCode : discountCodes) {
            // Count by status
            if (discountCode.isActive()) {
                if (discountCode.getExpiryDate() != null && discountCode.getExpiryDate().isBefore(today)) {
                    expiredCount++;
                } else {
                    activeCount++;
                }
            }
            
            if (discountCode.isUsed() || discountCode.getCurrentUsageCount() > 0) {
                usedCount++;
            }
            
            totalRedemptions += discountCode.getCurrentUsageCount();
            
            // Calculate discount given (simplified - you might want to get actual transaction data)
            if (discountCode.getDiscountType().toString().equals("CONSTANT")) {
                BigDecimal discountGiven = BigDecimal.valueOf(discountCode.getConstantDiscountAmount())
                        .multiply(BigDecimal.valueOf(discountCode.getCurrentUsageCount()));
                totalDiscountGiven = totalDiscountGiven.add(discountGiven);
            }
            // For percentage discounts, you'd need to calculate based on actual transaction amounts
        }
        
        summary.setActiveDiscountCodes(activeCount);
        summary.setUsedDiscountCodes(usedCount);
        summary.setExpiredDiscountCodes(expiredCount);
        summary.setTotalRedemptions(totalRedemptions);
        summary.setTotalDiscountGiven(totalDiscountGiven);
        
        // Calculate averages
        if (discountCodes.size() > 0) {
            summary.setAverageDiscountPerCode(totalDiscountGiven.divide(BigDecimal.valueOf(discountCodes.size()), 2, RoundingMode.HALF_UP));
        }
        
        if (totalRedemptions > 0) {
            summary.setAverageDiscountPerRedemption(totalDiscountGiven.divide(BigDecimal.valueOf(totalRedemptions), 2, RoundingMode.HALF_UP));
        }
        
        // Calculate redemption rate
        if (discountCodes.size() > 0) {
            BigDecimal redemptionRate = BigDecimal.valueOf(totalRedemptions)
                    .divide(BigDecimal.valueOf(discountCodes.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            summary.setRedemptionRate(redemptionRate);
        } else {
            summary.setRedemptionRate(BigDecimal.ZERO);
        }
        
        logger.debug("Discount code report completed - Total: {}, Active: {}, Used: {}, Redemptions: {}", 
                    discountCodes.size(), activeCount, usedCount, totalRedemptions);
        
        return summary;
    }

    private BatchReportDto.FinancialSummary generateFinancialSummary(Batch batch) {
        logger.debug("Generating financial summary for batch: {}", batch.getBatchNumber());
        
        BatchReportDto.FinancialSummary summary = new BatchReportDto.FinancialSummary();
        
        BigDecimal totalInitialValue = BigDecimal.ZERO;
        BigDecimal totalRemainingValue = BigDecimal.ZERO;
        BigDecimal totalUsedValue = BigDecimal.ZERO;
        BigDecimal totalDiscountGiven = BigDecimal.ZERO;
        
        if (batch.getBatchType() == Batch.BatchType.GIFT_CARD) {
            List<GiftCard> giftCards = giftCardRepository.findByBatchId(batch.getId());
            
            for (GiftCard giftCard : giftCards) {
                BigDecimal initialValue = BigDecimal.valueOf(giftCard.getInitialAmount());
                BigDecimal remainingValue = BigDecimal.valueOf(giftCard.getBalance());
                BigDecimal usedValue = initialValue.subtract(remainingValue);
                
                totalInitialValue = totalInitialValue.add(initialValue);
                totalRemainingValue = totalRemainingValue.add(remainingValue);
                totalUsedValue = totalUsedValue.add(usedValue);
            }
            
        } else if (batch.getBatchType() == Batch.BatchType.DISCOUNT_CODE) {
            List<DiscountCode> discountCodes = discountCodeRepository.findByBatchId(batch.getId());
            
            for (DiscountCode discountCode : discountCodes) {
                if (discountCode.getDiscountType().toString().equals("CONSTANT")) {
                    BigDecimal discountGiven = BigDecimal.valueOf(discountCode.getConstantDiscountAmount())
                            .multiply(BigDecimal.valueOf(discountCode.getCurrentUsageCount()));
                    totalDiscountGiven = totalDiscountGiven.add(discountGiven);
                }
            }
        }
        
        summary.setTotalInitialValue(totalInitialValue);
        summary.setTotalRemainingValue(totalRemainingValue);
        summary.setTotalUsedValue(totalUsedValue);
        summary.setTotalDiscountGiven(totalDiscountGiven);
        
        // Calculate overall utilization rate
        BigDecimal totalValue = totalInitialValue.add(totalDiscountGiven);
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal usedValue = totalUsedValue.add(totalDiscountGiven);
            BigDecimal utilizationRate = usedValue.divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            summary.setOverallUtilizationRate(utilizationRate);
        } else {
            summary.setOverallUtilizationRate(BigDecimal.ZERO);
        }
        
        // Calculate total financial impact
        summary.setTotalFinancialImpact(totalUsedValue.add(totalDiscountGiven));
        
        logger.debug("Financial summary completed - Initial: {}, Remaining: {}, Used: {}, Discount: {}", 
                    totalInitialValue, totalRemainingValue, totalUsedValue, totalDiscountGiven);
        
        return summary;
    }

    public List<BatchReportDto> generateAllBatchesReport() {
        logger.info("Generating report for all batches");
        
        List<Batch> batches = batchRepository.findAll();
        return batches.stream()
                .map(batch -> generateBatchReport(batch.getId()))
                .filter(report -> report != null)
                .toList();
    }

    public List<BatchReportDto> generateBatchesReportByCompany(Long companyId) {
        logger.info("Generating report for batches by company: {}", companyId);
        
        List<Batch> batches = batchRepository.findByCompanyId(companyId);
        return batches.stream()
                .map(batch -> generateBatchReport(batch.getId()))
                .filter(report -> report != null)
                .toList();
    }
}
