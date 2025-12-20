package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.GiftCardIssueRequest;
import com.pars.financial.dto.GiftCardReportDto;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.StoreLimitationRequest;
import com.pars.financial.service.GiftCardService;
import com.pars.financial.utils.ApiUserUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giftcard")
public class GiftCardController {

    private static final Logger logger = LoggerFactory.getLogger(GiftCardController.class);

    final GiftCardService giftCardService;

    public GiftCardController(GiftCardService giftCardService){
        this.giftCardService = giftCardService;
    }


    @GetMapping("/all")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardDto>>> getGiftCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        logger.info("GET /api/v1/giftcard/all called with pagination - page: {}, size: {}, companyId: {}, storeId: {}, sortBy: {}, sortDir: {}", 
                   page, size, companyId, storeId, sortBy, sortDir);
        
        var response = new GenericResponse<PagedResponse<GiftCardDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Get gift cards with RBAC and filtering
            PagedResponse<GiftCardDto> pagedGiftCards = giftCardService.getGiftCardsForCurrentUserWithFiltering(
                userResult.user, page, size, companyId, storeId, sortBy, sortDir);
            
            if (pagedGiftCards.getContent() == null || pagedGiftCards.getContent().isEmpty()) {
                logger.warn("Gift card list not found for user access level");
                response.status = -1;
                response.message = "No gift cards found for your access level";
            } else {
                response.message = "Gift cards retrieved successfully";
            }
            response.data = pagedGiftCards;
            
        } catch (Exception e) {
            logger.error("Error fetching gift cards with pagination: {}", e.getMessage());
            response.status = -1;
            response.message = "Error fetching gift cards: " + e.getMessage();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<GenericResponse<GiftCardDto>> getGiftCards(@PathVariable Long identifier){
        logger.info("GET /api/v1/giftcard/identifier/{} called", identifier);
        var response = new GenericResponse<GiftCardDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            var giftCard = giftCardService.getGiftCard(identifier);
            if (giftCard == null) {
                logger.warn("Gift card not found for identifier {}", identifier);
                response.status = -1;
                response.message = "Gift card not found";
            } else {
                // Check if user has access to this gift card
                if (!giftCardService.hasAccessToGiftCard(userResult.user, giftCard)) {
                    logger.warn("User {} does not have access to gift card {}", userResult.user.getUsername(), identifier);
                    response.status = 403;
                    response.message = "Access denied to this gift card";
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
                response.message = "Gift card retrieved successfully";
            }
            response.data = giftCard;
            
        } catch (Exception e) {
            logger.error("Error fetching gift card with identifier {}: {}", identifier, e.getMessage());
            response.status = -1;
            response.message = "Error fetching gift card: " + e.getMessage();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{serialNo}")
    public ResponseEntity<GenericResponse<GiftCardDto>> getGiftCards(@PathVariable String serialNo){
        logger.info("GET /api/v1/giftcard/{} called", serialNo);
        var response = new GenericResponse<GiftCardDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            var giftCard = giftCardService.getGiftCard(serialNo);
            if (giftCard == null) {
                logger.warn("Gift card not found for serialNo {}", serialNo);
                response.status = -1;
                response.message = "Gift card not found";
            } else {
                // Check if user has access to this gift card
                if (!giftCardService.hasAccessToGiftCard(userResult.user, giftCard)) {
                    logger.warn("User {} does not have access to gift card {}", userResult.user.getUsername(), serialNo);
                    response.status = 403;
                    response.message = "Access denied to this gift card";
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
                response.message = "Gift card retrieved successfully";
            }
            response.data = giftCard;
            
        } catch (Exception e) {
            logger.error("Error fetching gift card with serialNo {}: {}", serialNo, e.getMessage());
            response.status = -1;
            response.message = "Error fetching gift card: " + e.getMessage();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/issue")
    public GenericResponse<GiftCardDto> issueGiftCard(@RequestBody GiftCardIssueRequest dto){
        logger.info("POST /api/v1/giftcard/issue called with request: {}", dto);
        var genericResponseDto = new GenericResponse<GiftCardDto>();
        var gc = giftCardService.generateGiftCard(dto);
        genericResponseDto.data = gc ;
        return genericResponseDto;
    }

    @PostMapping("/issuelist")
    public GenericResponse<List<GiftCardDto>> issueGiftCards(@RequestBody GiftCardIssueRequest dto){
        logger.info("POST /api/v1/giftcard/issuelist called with request: {}", dto);
        var genericResponseDto = new GenericResponse<List<GiftCardDto>>();
        var gcl = giftCardService.generateGiftCards(dto);
        genericResponseDto.data = gcl ;
        return genericResponseDto;
    }

    @PostMapping("/register/{serialNo}")
    public ResponseEntity<GenericResponse<GiftCardDto>> registerGiftCard(@PathVariable String serialNo) {
        logger.info("POST /api/v1/giftcard/register/{} called", serialNo);
        var response = new GenericResponse<GiftCardDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Register gift card
            GiftCardDto giftCard = giftCardService.registerGiftCard(serialNo);
            response.data = giftCard;
            response.status = 200;
            response.message = "Gift card registered successfully";
            
            logger.info("Successfully registered gift card: {}", serialNo);
            return ResponseEntity.ok(response);
            
        } catch (com.pars.financial.exception.GiftCardNotFoundException e) {
            logger.warn("Gift card not found: {}", serialNo);
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (com.pars.financial.exception.ValidationException e) {
            logger.warn("Validation error registering gift card {}: {}", serialNo, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error registering gift card {}: {}", serialNo, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error registering gift card {}: {}", serialNo, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to register gift card: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{serialNo}/limit-stores")
    public GenericResponse<Void> limitToStores(@PathVariable String serialNo, @RequestBody StoreLimitationRequest request) {
        logger.info("POST /api/v1/giftcard/{}/limit-stores called with storeIds: {}", serialNo, request.storeIds);
        var response = new GenericResponse<Void>();
        giftCardService.limitToStores(serialNo, request.storeIds);
        return response;
    }

    @PostMapping("/{serialNo}/remove-store-limitation")
    public GenericResponse<Void> removeStoreLimitation(@PathVariable String serialNo) {
        logger.info("POST /api/v1/giftcard/{}/remove-store-limitation called", serialNo);
        var response = new GenericResponse<Void>();
        giftCardService.removeStoreLimitation(serialNo);
        return response;
    }

    @PostMapping("/{serialNo}/limit-item-categories")
    public GenericResponse<Void> limitToItemCategories(@PathVariable String serialNo, @RequestBody StoreLimitationRequest request) {
        logger.info("POST /api/v1/giftcard/{}/limit-item-categories called with itemCategoryIds: {}", serialNo, request.storeIds);
        var response = new GenericResponse<Void>();
        giftCardService.limitToItemCategories(serialNo, request.storeIds);
        return response;
    }

    @PostMapping("/{serialNo}/remove-item-category-limitation")
    public GenericResponse<Void> removeItemCategoryLimitation(@PathVariable String serialNo) {
        logger.info("POST /api/v1/giftcard/{}/remove-item-category-limitation called", serialNo);
        var response = new GenericResponse<Void>();
        giftCardService.removeItemCategoryLimitation(serialNo);
        return response;
    }

    @GetMapping("/company/{companyId}")
    public GenericResponse<PagedResponse<GiftCardDto>> getGiftCardsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/company/{} called with pagination - page: {}, size: {}", companyId, page, size);
        var response = new GenericResponse<PagedResponse<GiftCardDto>>();
        try {
            PagedResponse<GiftCardDto> pagedGiftCards = giftCardService.getGiftCardsByCompany(companyId, page, size);
            response.data = pagedGiftCards;
        } catch (Exception e) {
            logger.error("Error fetching gift cards for company {}: {}", companyId, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/{serialNo}/assign-company/{companyId}")
    public GenericResponse<GiftCardDto> assignCompanyToGiftCard(@PathVariable String serialNo, @PathVariable Long companyId) {
        logger.info("POST /api/v1/giftcard/{}/assign-company/{} called", serialNo, companyId);
        var response = new GenericResponse<GiftCardDto>();
        try {
            var giftCard = giftCardService.assignCompanyToGiftCard(serialNo, companyId);
            response.data = giftCard;
        } catch (Exception e) {
            logger.error("Error assigning company {} to gift card {}: {}", companyId, serialNo, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/{serialNo}/remove-company")
    public GenericResponse<GiftCardDto> removeCompanyFromGiftCard(@PathVariable String serialNo) {
        logger.info("POST /api/v1/giftcard/{}/remove-company called", serialNo);
        var response = new GenericResponse<GiftCardDto>();
        try {
            var giftCard = giftCardService.removeCompanyFromGiftCard(serialNo);
            response.data = giftCard;
        } catch (Exception e) {
            logger.error("Error removing company from gift card {}: {}", serialNo, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/report")
    public ResponseEntity<GenericResponse<GiftCardReportDto>> getGiftCardReport() {
        logger.info("GET /api/v1/giftcard/report called");
        var response = new GenericResponse<GiftCardReportDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Generate report
            GiftCardReportDto report = giftCardService.generateGiftCardReport();
            response.data = report;
            response.status = 200;
            response.message = "Gift card report generated successfully";
            
            logger.info("Successfully generated gift card report");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating gift card report: {}", e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to generate gift card report: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report/company/{companyId}")
    public ResponseEntity<GenericResponse<GiftCardReportDto>> getGiftCardReportByCompany(@PathVariable Long companyId) {
        logger.info("GET /api/v1/giftcard/report/company/{} called", companyId);
        var response = new GenericResponse<GiftCardReportDto>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Generate company-specific report
            GiftCardReportDto report = giftCardService.generateGiftCardReportByCompany(companyId);
            response.data = report;
            response.status = 200;
            response.message = "Gift card report for company " + companyId + " generated successfully";
            
            logger.info("Successfully generated gift card report for company: {}", companyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating gift card report for company {}: {}", companyId, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to generate gift card report for company: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardDto>>> getGiftCardsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/customer/{} called with pagination - page: {}, size: {}", customerId, page, size);
        var response = new GenericResponse<PagedResponse<GiftCardDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Get gift cards for customer
            PagedResponse<GiftCardDto> pagedGiftCards = giftCardService.getGiftCardsByCustomer(customerId, page, size);
            
            if (pagedGiftCards.getContent() == null || pagedGiftCards.getContent().isEmpty()) {
                logger.debug("No gift cards found for customer: {}", customerId);
                response.status = 0;
                response.message = "No gift cards found for this customer";
            } else {
                response.message = "Gift cards retrieved successfully";
            }
            response.data = pagedGiftCards;
            
        } catch (com.pars.financial.exception.ValidationException e) {
            logger.warn("Validation error fetching gift cards for customer {}: {}", customerId, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error fetching gift cards for customer {}: {}", customerId, e.getMessage(), e);
            response.status = -1;
            response.message = "Error fetching gift cards: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/my")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardDto>>> getMyGiftCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/customer/my called with pagination - page: {}, size: {}", page, size);
        var response = new GenericResponse<PagedResponse<GiftCardDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Get gift cards for current user's customer (customerId = null means use current user)
            PagedResponse<GiftCardDto> pagedGiftCards = giftCardService.getGiftCardsByCustomer(null, page, size);
            
            if (pagedGiftCards.getContent() == null || pagedGiftCards.getContent().isEmpty()) {
                logger.debug("No gift cards found for current user");
                response.status = 0;
                response.message = "No gift cards found";
            } else {
                response.message = "Gift cards retrieved successfully";
            }
            response.data = pagedGiftCards;
            
        } catch (com.pars.financial.exception.ValidationException e) {
            logger.warn("Validation error fetching gift cards for current user: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error fetching gift cards for current user: {}", e.getMessage(), e);
            response.status = -1;
            response.message = "Error fetching gift cards: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/block/{serialNo}")
    public ResponseEntity<GenericResponse<GiftCardDto>> blockGiftCard(
            @PathVariable String serialNo) {
        logger.info("PUT /api/v1/giftcard/block/{} called", serialNo);
        var response = new GenericResponse<GiftCardDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            GiftCardDto giftCard = giftCardService.blockGiftCard(serialNo, true);
            response.data = giftCard;
            response.status = 200;
            response.message = "Gift card blocked successfully";

            logger.info("Successfully blocked gift card: {}", serialNo);
            return ResponseEntity.ok(response);

        } catch (com.pars.financial.exception.GiftCardNotFoundException e) {
            logger.warn("Gift card not found: {}", serialNo);
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (com.pars.financial.exception.ValidationException e) {
            logger.warn("Validation error blocking gift card {}: {}", serialNo, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error blocking gift card {}: {}", serialNo, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error blocking gift card {}: {}", serialNo, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to block gift card: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/unblock/{serialNo}")
    public ResponseEntity<GenericResponse<GiftCardDto>> unblockGiftCard(
            @PathVariable String serialNo) {
        logger.info("PUT /api/v1/giftcard/unblock/{} called", serialNo);
        var response = new GenericResponse<GiftCardDto>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            GiftCardDto giftCard = giftCardService.blockGiftCard(serialNo, false);
            response.data = giftCard;
            response.status = 200;
            response.message = "Gift card unblocked successfully";

            logger.info("Successfully unblocked gift card: {}", serialNo);
            return ResponseEntity.ok(response);

        } catch (com.pars.financial.exception.GiftCardNotFoundException e) {
            logger.warn("Gift card not found: {}", serialNo);
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (com.pars.financial.exception.ValidationException e) {
            logger.warn("Validation error unblocking gift card {}: {}", serialNo, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            logger.warn("Authentication error unblocking gift card {}: {}", serialNo, e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error unblocking gift card {}: {}", serialNo, e.getMessage(), e);
            response.status = -1;
            response.message = "Failed to unblock gift card: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
