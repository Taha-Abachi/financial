package com.pars.financial.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.SettlementReportDto;
import com.pars.financial.service.SettlementService;

@RestController
@RequestMapping("/api/v1/settlement")
public class SettlementController {

    private static final Logger logger = LoggerFactory.getLogger(SettlementController.class);

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * Get settlement report for fulfilled gift card transactions
     * 
     * @param startDate Start date (required, format: yyyy-MM-dd)
     * @param endDate End date (required, format: yyyy-MM-dd, exclusive)
     * @param storeId Optional store filter
     * @param companyId Optional company filter (for COMPANY_USER, must match their company or will be auto-set)
     * @return Settlement report with master and detail data
     */
    @GetMapping("/report")
    public ResponseEntity<GenericResponse<SettlementReportDto>> getSettlementReport(
            @RequestParam(value = "startDate", required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "companyId", required = false) Long companyId) {
        
        logger.info("GET /api/v1/settlement/report called - startDate: {}, endDate: {}, storeId: {}, companyId: {}", 
                   startDate, endDate, storeId, companyId);

        var response = new GenericResponse<SettlementReportDto>();

        try {
            // Validate date range
            if (startDate.isAfter(endDate)) {
                response.status = -1;
                response.message = "Start date must be before or equal to end date";
                response.messageFa = "تاریخ شروع باید قبل از یا برابر با تاریخ پایان باشد";
                return ResponseEntity.badRequest().body(response);
            }

            // Generate settlement report
            SettlementReportDto report = settlementService.generateSettlementReport(startDate, endDate, storeId, companyId);

            response.status = 1;
            response.message = "Settlement report generated successfully";
            response.messageFa = "گزارش تسویه با موفقیت ایجاد شد";
            response.data = report;

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in settlement report request: {}", e.getMessage());
            response.status = -1;
            response.message = "Invalid request parameters: " + e.getMessage();
            response.messageFa = "پارامترهای درخواست نامعتبر است";
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error generating settlement report: {}", e.getMessage(), e);
            response.status = -1;
            response.message = "Error generating settlement report: " + e.getMessage();
            response.messageFa = "خطا در ایجاد گزارش تسویه";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

