package com.pars.financial.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.dto.DiscountCodeReportDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.StoreLimitationRequest;
import com.pars.financial.service.DiscountCodeService;
import com.pars.financial.utils.ApiUserUtil;

@RestController
@RequestMapping("/api/v1/discountcode")
public class DiscountCodeController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeController.class);

    private final DiscountCodeService codeService;

    public DiscountCodeController(DiscountCodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("/{code}")
    public GenericResponse<DiscountCodeDto> discountCode(@PathVariable String code) {
        logger.info("GET /api/v1/discountcode/{} called", code);
        GenericResponse<DiscountCodeDto> response = new GenericResponse<>();
        var dto = codeService.getDiscountCode(code);
        if(dto == null){
            logger.warn("Discount code not found for code {}", code);
            response.status = -1;
            response.message = "Discount code not found";
        }
        response.data = dto;
        return response;
    }

    @PostMapping("/issue")
    public GenericResponse<DiscountCodeDto> issueDiscountCode(@RequestBody DiscountCodeIssueRequest discountCodeDto) {
        logger.info("POST /api/v1/discountcode/issue called with request: {}", discountCodeDto);
        GenericResponse<DiscountCodeDto> response = new GenericResponse<>();
        var dto = codeService.generate(discountCodeDto);
        response.data = dto;
        return response;
    }

    @PostMapping("/issuelist")
    public GenericResponse<List<DiscountCodeDto>> redeemDiscountCode(@RequestBody DiscountCodeIssueRequest discountCodeDto) {
        logger.info("POST /api/v1/discountcode/issuelist called with request: {}", discountCodeDto);
        GenericResponse<List<DiscountCodeDto>> response = new GenericResponse<>();
        response.data = codeService.generateList(discountCodeDto);
        return response;
    }



    @PostMapping("/{code}/limit-stores")
    public GenericResponse<Void> limitToStores(@PathVariable String code, @RequestBody StoreLimitationRequest request) {
        logger.info("POST /api/v1/discountcode/{}/limit-stores called with storeIds: {}", code, request.storeIds);
        var response = new GenericResponse<Void>();
        codeService.limitToStores(code, request.storeIds);
        return response;
    }

    @PostMapping("/{code}/remove-store-limitation")
    public GenericResponse<Void> removeStoreLimitation(@PathVariable String code) {
        logger.info("POST /api/v1/discountcode/{}/remove-store-limitation called", code);
        var response = new GenericResponse<Void>();
        codeService.removeStoreLimitation(code);
        return response;
    }

    @PostMapping("/{code}/limit-item-categories")
    public GenericResponse<Void> limitToItemCategories(@PathVariable String code, @RequestBody StoreLimitationRequest request) {
        logger.info("POST /api/v1/discountcode/{}/limit-item-categories called with itemCategoryIds: {}", code, request.storeIds);
        var response = new GenericResponse<Void>();
        codeService.limitToItemCategories(code, request.storeIds);
        return response;
    }

    @PostMapping("/{code}/remove-item-category-limitation")
    public GenericResponse<Void> removeItemCategoryLimitation(@PathVariable String code) {
        logger.info("POST /api/v1/discountcode/{}/remove-item-category-limitation called", code);
        var response = new GenericResponse<Void>();
        codeService.removeItemCategoryLimitation(code);
        return response;
    }

    @GetMapping("/report")
    public ResponseEntity<GenericResponse<DiscountCodeReportDto>> getDiscountCodeReport() {
        logger.info("GET /api/v1/discountcode/report called");
        var response = new GenericResponse<DiscountCodeReportDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Generate report
            DiscountCodeReportDto report = codeService.generateDiscountCodeReport();
            response.data = report;
            response.status = 200;
            response.message = "Discount code report generated successfully";
            
            logger.info("Successfully generated discount code report");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating discount code report: {}", e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to generate discount code report: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report/company/{companyId}")
    public ResponseEntity<GenericResponse<DiscountCodeReportDto>> getDiscountCodeReportByCompany(@PathVariable Long companyId) {
        logger.info("GET /api/v1/discountcode/report/company/{} called", companyId);
        var response = new GenericResponse<DiscountCodeReportDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Generate company-specific report
            DiscountCodeReportDto report = codeService.generateDiscountCodeReportByCompany(companyId);
            response.data = report;
            response.status = 200;
            response.message = "Discount code report for company " + companyId + " generated successfully";
            
            logger.info("Successfully generated discount code report for company: {}", companyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating discount code report for company {}: {}", companyId, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to generate discount code report for company: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
