package com.pars.financial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.service.DataCleansingService;
import com.pars.financial.service.DataCleansingService.DataCleansingResult;
import com.pars.financial.service.DataCleansingService.DataInconsistencyReport;

@RestController
@RequestMapping("/api/v1/admin/data-cleansing")
public class DataCleansingController {

    private static final Logger logger = LoggerFactory.getLogger(DataCleansingController.class);

    private final DataCleansingService dataCleansingService;

    public DataCleansingController(DataCleansingService dataCleansingService) {
        this.dataCleansingService = dataCleansingService;
    }

    /**
     * Generate a report of data inconsistencies without fixing anything
     * This is a safe operation that only reads data
     */
    @GetMapping("/report")
    public GenericResponse<DataInconsistencyReport> generateInconsistencyReport() {
        logger.info("GET /api/v1/admin/data-cleansing/report called");
        
        GenericResponse<DataInconsistencyReport> response = new GenericResponse<>();
        
        try {
            DataInconsistencyReport report = dataCleansingService.generateInconsistencyReport();
            response.data = report;
            
            if (report.hasInconsistencies()) {
                response.message = "Data inconsistencies found. Consider running the cleanse operation.";
                logger.warn("Data inconsistencies found: {} total issues", report.getTotalInconsistencies());
            } else {
                response.message = "No data inconsistencies found. Database is clean.";
                logger.info("No data inconsistencies found");
            }
            
        } catch (Exception e) {
            logger.error("Error generating inconsistency report", e);
            response.status = -1;
            response.message = "Error generating report: " + e.getMessage();
        }
        
        return response;
    }

    /**
     * Cleanse gift card transaction data by fixing inconsistent statuses
     * This operation modifies the database and should be used with caution
     */
    @PostMapping("/cleanse-giftcard-transactions")
    public GenericResponse<DataCleansingResult> cleanseGiftCardTransactions() {
        logger.info("POST /api/v1/admin/data-cleansing/cleanse-giftcard-transactions called");
        
        GenericResponse<DataCleansingResult> response = new GenericResponse<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            DataCleansingResult result = dataCleansingService.cleanseGiftCardTransactions();
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            
            response.data = result;
            
            if (result.isSuccess()) {
                if (result.getTotalFixed() > 0) {
                    response.message = String.format("Data cleansing completed successfully. Fixed %d transactions in %d ms", 
                        result.getTotalFixed(), result.getExecutionTime());
                    logger.info("Data cleansing completed successfully. Fixed {} transactions", result.getTotalFixed());
                } else {
                    response.message = "Data cleansing completed. No issues found to fix.";
                    logger.info("Data cleansing completed. No issues found to fix.");
                }
            } else {
                response.status = -1;
                response.message = "Data cleansing failed: " + result.getErrorMessage();
                logger.error("Data cleansing failed: {}", result.getErrorMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error during data cleansing", e);
            response.status = -1;
            response.message = "Error during data cleansing: " + e.getMessage();
        }
        
        return response;
    }

    /**
     * Generate a comprehensive data health report
     * This combines both the inconsistency report and provides recommendations
     */
    @GetMapping("/health-report")
    public GenericResponse<DataHealthReport> generateDataHealthReport() {
        logger.info("GET /api/v1/admin/data-cleansing/health-report called");
        
        GenericResponse<DataHealthReport> response = new GenericResponse<>();
        
        try {
            DataInconsistencyReport inconsistencyReport = dataCleansingService.generateInconsistencyReport();
            
            DataHealthReport healthReport = new DataHealthReport();
            healthReport.setInconsistencyReport(inconsistencyReport);
            healthReport.setOverallHealth(inconsistencyReport.hasInconsistencies() ? "UNHEALTHY" : "HEALTHY");
            healthReport.setRecommendations(generateRecommendations(inconsistencyReport));
            
            response.data = healthReport;
            response.message = "Data health report generated successfully";
            
        } catch (Exception e) {
            logger.error("Error generating data health report", e);
            response.status = -1;
            response.message = "Error generating health report: " + e.getMessage();
        }
        
        return response;
    }

    /**
     * Generate recommendations based on the inconsistency report
     */
    private String[] generateRecommendations(DataInconsistencyReport report) {
        if (!report.hasInconsistencies()) {
            return new String[]{"Database is healthy. No action required."};
        }
        
        StringBuilder recommendations = new StringBuilder();
        
        if (report.getPendingWithConfirmations() > 0) {
            recommendations.append("Fix pending debit transactions with confirmations. ");
        }
        
        if (report.getPendingWithReversals() > 0) {
            recommendations.append("Fix pending debit transactions with reversals. ");
        }
        
        if (report.getPendingWithRefunds() > 0) {
            recommendations.append("Fix pending debit transactions with refunds. ");
        }
        
        if (report.getOrphanedSettlements() > 0) {
            recommendations.append("Investigate orphaned settlement transactions. ");
        }
        
        recommendations.append("Consider running the data cleansing operation to fix these issues automatically.");
        
        return new String[]{recommendations.toString()};
    }

    /**
     * Data health report class
     */
    public static class DataHealthReport {
        private String overallHealth;
        private DataInconsistencyReport inconsistencyReport;
        private String[] recommendations;
        private long reportGeneratedAt;

        public DataHealthReport() {
            this.reportGeneratedAt = System.currentTimeMillis();
        }

        // Getters and setters
        public String getOverallHealth() { return overallHealth; }
        public void setOverallHealth(String overallHealth) { this.overallHealth = overallHealth; }
        
        public DataInconsistencyReport getInconsistencyReport() { return inconsistencyReport; }
        public void setInconsistencyReport(DataInconsistencyReport inconsistencyReport) { this.inconsistencyReport = inconsistencyReport; }
        
        public String[] getRecommendations() { return recommendations; }
        public void setRecommendations(String[] recommendations) { this.recommendations = recommendations; }
        
        public long getReportGeneratedAt() { return reportGeneratedAt; }
        public void setReportGeneratedAt(long reportGeneratedAt) { this.reportGeneratedAt = reportGeneratedAt; }
    }
}
