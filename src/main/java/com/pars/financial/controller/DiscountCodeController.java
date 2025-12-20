package com.pars.financial.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.dto.DiscountCodeReportDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.StoreLimitationRequest;
import com.pars.financial.service.DiscountCodeService;
import com.pars.financial.utils.ApiUserUtil;

import jakarta.validation.constraints.Pattern;

import com.pars.financial.exception.ValidationException;

@RestController
@RequestMapping("/api/v1/discountcode")
public class DiscountCodeController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeController.class);

    private final DiscountCodeService codeService;

    public DiscountCodeController(DiscountCodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("/all")
    public ResponseEntity<GenericResponse<PagedResponse<DiscountCodeDto>>> getAllDiscountCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        logger.info("GET /api/v1/discountcode/all called with pagination - page: {}, size: {}, companyId: {}, storeId: {}, sortBy: {}, sortDir: {}",
                   page, size, companyId, storeId, sortBy, sortDir);

        var response = new GenericResponse<PagedResponse<DiscountCodeDto>>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            PagedResponse<DiscountCodeDto> pagedDiscountCodes = codeService.getDiscountCodesForCurrentUserWithFiltering(
                userResult.user, page, size, companyId, storeId, sortBy, sortDir);

            if (pagedDiscountCodes.getContent() == null || pagedDiscountCodes.getContent().isEmpty()) {
                logger.warn("Discount code list not found for user access level");
                response.status = -1;
                response.message = "No discount codes found for your access level";
            } else {
                response.message = "Discount codes retrieved successfully";
            }
            response.data = pagedDiscountCodes;

        } catch (Exception e) {
            logger.error("Error fetching discount codes with pagination: {}", e.getMessage());
            response.status = -1;
            response.message = "Error fetching discount codes: " + e.getMessage();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<GenericResponse<DiscountCodeDto>> discountCode(@PathVariable String code) {
        logger.info("GET /api/v1/discountcode/{} called", code);
        var response = new GenericResponse<DiscountCodeDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            var dto = codeService.getDiscountCode(code);
            if (dto == null) {
                logger.warn("Discount code not found for code {}", code);
                response.status = -1;
                response.message = "Discount code not found";
                return ResponseEntity.ok(response);
            }

            // Check RBAC access
            if (!codeService.hasAccessToDiscountCode(userResult.user, dto)) {
                logger.warn("User {} does not have access to discount code {}", userResult.user.getUsername(), code);
                response.status = 403;
                response.message = "Access denied to this discount code";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.data = dto;
            response.message = "Discount code retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching discount code {}: {}", code, e.getMessage());
            response.status = -1;
            response.message = "Error fetching discount code: " + e.getMessage();
            return ResponseEntity.ok(response);
        }
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

    @PutMapping("/block/{code}")
    public ResponseEntity<GenericResponse<DiscountCodeDto>> blockDiscountCode(
            @PathVariable String code) {
        logger.info("PUT /api/v1/discountcode/block/{} called", code);
        var response = new GenericResponse<DiscountCodeDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            DiscountCodeDto discountCode = codeService.blockDiscountCode(code, true);
            response.data = discountCode;
            response.status = 200;
            response.message = "Discount code blocked successfully";

            logger.info("Successfully blocked discount code: {}", code);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Validation error blocking discount code {}: {}", code, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error blocking discount code {}: {}", code, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error blocking discount code {}: {}", code, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to block discount code: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/unblock/{code}")
    public ResponseEntity<GenericResponse<DiscountCodeDto>> unblockDiscountCode(
            @PathVariable String code) {
        logger.info("PUT /api/v1/discountcode/unblock/{} called", code);
        var response = new GenericResponse<DiscountCodeDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            DiscountCodeDto discountCode = codeService.blockDiscountCode(code, false);
            response.data = discountCode;
            response.status = 200;
            response.message = "Discount code unblocked successfully";

            logger.info("Successfully unblocked discount code: {}", code);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Validation error unblocking discount code {}: {}", code, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error unblocking discount code {}: {}", code, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error unblocking discount code {}: {}", code, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to unblock discount code: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/deactivate/{code}")
    public ResponseEntity<GenericResponse<DiscountCodeDto>> deactivateDiscountCode(
            @PathVariable String code) {
        logger.info("PUT /api/v1/discountcode/deactivate/{} called", code);
        var response = new GenericResponse<DiscountCodeDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            DiscountCodeDto discountCode = codeService.activateDiscountCode(code, false);
            response.data = discountCode;
            response.status = 200;
            response.message = "Discount code deactivated successfully";

            logger.info("Successfully deactivated discount code: {}", code);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Validation error deactivating discount code {}: {}", code, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error deactivating discount code {}: {}", code, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error deactivating discount code {}: {}", code, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to deactivate discount code: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/activate/{code}")
    public ResponseEntity<GenericResponse<DiscountCodeDto>> activateDiscountCode(
            @PathVariable String code) {
        logger.info("PUT /api/v1/discountcode/activate/{} called", code);
        var response = new GenericResponse<DiscountCodeDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            DiscountCodeDto discountCode = codeService.activateDiscountCode(code, true);
            response.data = discountCode;
            response.status = 200;
            response.message = "Discount code activated successfully";

            logger.info("Successfully activated discount code: {}", code);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Validation error activating discount code {}: {}", code, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error activating discount code {}: {}", code, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error activating discount code {}: {}", code, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to activate discount code: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/personal/{phoneNumber}")
    public ResponseEntity<GenericResponse<List<DiscountCodeDto>>> getPersonalDiscountCodesByPhoneNumber(
            @PathVariable("phoneNumber") @Pattern(regexp = "^\\d{11}$") String phoneNumber) {
        logger.info("GET /api/v1/discountcode/personal/{} called", phoneNumber);
        var response = new GenericResponse<List<DiscountCodeDto>>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            List<DiscountCodeDto> personalCodes = codeService.getPersonalDiscountCodesByPhoneNumber(
                userResult.user, phoneNumber);
            
            response.data = personalCodes;
            response.status = 200;
            response.message = "Personal discount codes retrieved successfully";
            
            logger.info("Successfully retrieved {} personal discount codes for phone number: {}", 
                       personalCodes.size(), phoneNumber);
            return ResponseEntity.ok(response);

        } catch (ValidationException e) {
            logger.warn("Validation error retrieving personal discount codes for phone number {}: {}", 
                       phoneNumber, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error retrieving personal discount codes for phone number {}: {}", 
                        phoneNumber, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to retrieve personal discount codes: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
