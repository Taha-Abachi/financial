package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.TransactionAggregationResponseDto;
import com.pars.financial.service.GiftCardTransactionService;
import com.pars.financial.service.GiftCardService;
import com.pars.financial.utils.ApiUserUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/giftcard/transaction")
public class GiftCardTransactionController {
    private static final Logger logger = LoggerFactory.getLogger(GiftCardTransactionController.class);
    final GiftCardTransactionService transactionService;
    final GiftCardService giftCardService;

    public GiftCardTransactionController(GiftCardTransactionService transactionService, GiftCardService giftCardService) {
        this.transactionService = transactionService;
        this.giftCardService = giftCardService;
    }

    @PostMapping("/debit")
    public ResponseEntity<GenericResponse<GiftCardTransactionDto>> debit(@RequestBody GiftCardTransactionDto dto) {
        logger.info("POST /api/v1/giftcard/transaction/debit called with request: {}", dto);
        var res = new GenericResponse<GiftCardTransactionDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            res.message = userResult.errorMessage;
            res.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(res);
        }

        var trx = transactionService.debitGiftCard(userResult.user, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.storeId, dto.phoneNo, dto.orderAmount);
        if (trx == null) {
            logger.warn("Failed to debit gift card for clientTransactionId: {}", dto.clientTransactionId);
            res.message = "Failed to debit gift card";
            res.status = -1;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        res.data = trx;
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reverse")
    public ResponseEntity<GenericResponse<GiftCardTransactionDto>> reverse(@RequestBody GiftCardTransactionDto dto) {
        logger.info("POST /api/v1/giftcard/transaction/reverse called with request: {}", dto);
        var res = new GenericResponse<GiftCardTransactionDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            res.message = userResult.errorMessage;
            res.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(res);
        }
        
        var trx = transactionService.reverseTransaction(userResult.user, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId, dto.orderAmount);
        if (trx == null) {
            logger.warn("Failed to reverse gift card transaction for clientTransactionId: {}", dto.clientTransactionId);
            res.message = "Failed to reverse gift card transaction.";
            res.status = -1;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        res.data = trx;
        return ResponseEntity.ok(res);
    }

    @PostMapping("/confirm")
    public ResponseEntity<GenericResponse<GiftCardTransactionDto>> confirm(@RequestBody GiftCardTransactionDto dto) {
        logger.info("POST /api/v1/giftcard/transaction/confirm called with request: {}", dto);
        var res = new GenericResponse<GiftCardTransactionDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            res.message = userResult.errorMessage;
            res.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(res);
        }
        
        var trx = transactionService.confirmTransaction(userResult.user, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId, dto.orderAmount);
        if (trx == null) {
            logger.warn("Failed to confirm gift card transaction for clientTransactionId: {}", dto.clientTransactionId);
            res.message = "Failed to confirm gift card transaction.";
            res.status = -1;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        res.data = trx;
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refund")
    public ResponseEntity<GenericResponse<GiftCardTransactionDto>> refund(@RequestBody GiftCardTransactionDto dto) {
        logger.info("POST /api/v1/giftcard/transaction/refund called with request: {}", dto);
        var res = new GenericResponse<GiftCardTransactionDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            res.message = userResult.errorMessage;
            res.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(res);
        }
        
        var trx = transactionService.refundTransaction(userResult.user, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId, dto.orderAmount);
        if (trx == null) {
            logger.warn("Failed to refund gift card transaction for clientTransactionId: {}", dto.clientTransactionId);
            res.message = "Failed to refund gift card transaction.";
            res.status = -1;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        res.data = trx;
        return ResponseEntity.ok(res);
    }

    @GetMapping("/checkStatus/{clientTransactionId}")
    public GenericResponse<GiftCardTransactionDto> checkTransactionStatus(@PathVariable String clientTransactionId) {
        logger.info("GET /api/v1/giftcard/transaction/checkStatus/{} called", clientTransactionId);
        var res = new GenericResponse<GiftCardTransactionDto>();
        var trx = transactionService.checkStatus(clientTransactionId);
        if (trx == null) {
            logger.warn("Failed to find gift card transaction for clientTransactionId: {}", clientTransactionId);
            res.message = "Failed to find gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/{transactionId}")
    public GenericResponse<GiftCardTransactionDto> getTransaction(@PathVariable String transactionId) {
        logger.info("GET /api/v1/giftcard/transaction/{} called", transactionId);
        var res = new GenericResponse<GiftCardTransactionDto>();
        var trx = transactionService.get(transactionId);
        if (trx == null) {
            logger.warn("Failed to find gift card transaction for transactionId: {}", transactionId);
            res.message = "Failed to find gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/list/{serialNo}")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardTransactionDto>>> getTransactionHistory(
            @PathVariable String serialNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/transaction/list/{} called with pagination - page: {}, size: {}", serialNo, page, size);
        var response = new GenericResponse<PagedResponse<GiftCardTransactionDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Check if user has access to the gift card
            var giftCard = giftCardService.getGiftCard(serialNo);
            if (giftCard == null) {
                logger.warn("Gift card not found for serialNo: {}", serialNo);
                response.message = "Gift card not found";
                response.status = -1;
                return ResponseEntity.ok(response);
            }
            
            if (!giftCardService.hasAccessToGiftCard(userResult.user, giftCard)) {
                logger.warn("User {} does not have access to gift card {}", userResult.user.getUsername(), serialNo);
                response.status = 403;
                response.message = "Access denied to this gift card";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            PagedResponse<GiftCardTransactionDto> pagedTransactions = transactionService.getTransactionHistory(serialNo, page, size);
            response.data = pagedTransactions;
            response.message = "Transaction history retrieved successfully";
            
        } catch (Exception e) {
            logger.error("Error fetching transaction history for gift card {}: {}", serialNo, e.getMessage());
            response.message = "Failed to find gift card: " + e.getMessage();
            response.status = -1;
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardTransactionDto>>> getGiftCardsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/transaction/company/{} called with pagination - page: {}, size: {}", companyId, page, size);
        var response = new GenericResponse<PagedResponse<GiftCardTransactionDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Check if user has access to this company
            if (!transactionService.hasAccessToCompany(userResult.user, companyId)) {
                logger.warn("User {} does not have access to company {}", userResult.user.getUsername(), companyId);
                response.status = 403;
                response.message = "Access denied to this company";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            PagedResponse<GiftCardTransactionDto> pagedTransactions = transactionService.getGiftCardsByCompany(companyId, page, size);
            response.data = pagedTransactions;
            response.message = "Company gift cards retrieved successfully";
            
        } catch (Exception e) {
            logger.error("Error fetching gift cards for company {}: {}", companyId, e.getMessage());
            response.message = e.getMessage();
            response.status = -1;
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/history")
    public ResponseEntity<GenericResponse<PagedResponse<GiftCardTransactionDto>>> getCompanyTransactionHistory(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/v1/giftcard/transaction/company/{}/history called with pagination - page: {}, size: {}", companyId, page, size);
        var response = new GenericResponse<PagedResponse<GiftCardTransactionDto>>();
        
        try {
            // Check authentication
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }
            
            // Check if user has access to this company
            if (!transactionService.hasAccessToCompany(userResult.user, companyId)) {
                logger.warn("User {} does not have access to company {}", userResult.user.getUsername(), companyId);
                response.status = 403;
                response.message = "Access denied to this company";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            PagedResponse<GiftCardTransactionDto> pagedTransactions = transactionService.getCompanyTransactionHistory(companyId, page, size);
            response.data = pagedTransactions;
            response.message = "Company transaction history retrieved successfully";
            
        } catch (Exception e) {
            logger.error("Error fetching transaction history for company {}: {}", companyId, e.getMessage());
            response.message = e.getMessage();
            response.status = -1;
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aggregations")
    public ResponseEntity<GenericResponse<TransactionAggregationResponseDto>> getTransactionAggregations(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long storeId) {
        logger.info("GET /api/v1/giftcard/transaction/aggregations called with companyId: {}, storeId: {}", companyId, storeId);
        var response = new GenericResponse<TransactionAggregationResponseDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            response.message = userResult.errorMessage;
            response.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(response);
        }
        
        try {
            var aggregations = transactionService.getTransactionAggregationsWithFiltering(userResult.user, companyId, storeId);
            response.data = aggregations;
            response.message = "Transaction aggregations retrieved successfully";
            logger.info("Successfully retrieved transaction aggregations for user: {}", userResult.user.getUsername());
        } catch (Exception e) {
            logger.error("Error getting transaction aggregations: {}", e.getMessage());
            response.message = "Failed to retrieve transaction aggregations";
            response.status = -1;
        }
        
        return ResponseEntity.ok(response);
    }

}
