package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.service.GiftCardTransactionService;
import com.pars.financial.utils.ApiUserUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giftcard/transaction")
public class GiftCardTransactionController {
    private static final Logger logger = LoggerFactory.getLogger(GiftCardTransactionController.class);
    final GiftCardTransactionService transactionService;

    public GiftCardTransactionController(GiftCardTransactionService transactionService) {
        this.transactionService = transactionService;
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
    public GenericResponse<List<GiftCardTransactionDto>> getTransactionHistory(@PathVariable String serialNo) {
        logger.info("GET /api/v1/giftcard/transaction/list/{} called", serialNo);
        var res = new GenericResponse<List<GiftCardTransactionDto>>();
        var trx = transactionService.getTransactionHistory(serialNo);
        if (trx == null) {
            logger.warn("Failed to find gift card for serialNo: {}", serialNo);
            res.message = "Failed to find gift card.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/company/{companyId}")
    public GenericResponse<List<GiftCardTransactionDto>> getGiftCardsByCompany(@PathVariable Long companyId) {
        logger.info("GET /api/v1/giftcard/transaction/company/{} called", companyId);
        var res = new GenericResponse<List<GiftCardTransactionDto>>();
        try {
            var transactions = transactionService.getGiftCardsByCompany(companyId);
            res.data = transactions;
        } catch (Exception e) {
            logger.error("Error fetching gift cards for company {}: {}", companyId, e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @GetMapping("/company/{companyId}/history")
    public GenericResponse<List<GiftCardTransactionDto>> getCompanyTransactionHistory(@PathVariable Long companyId) {
        logger.info("GET /api/v1/giftcard/transaction/company/{}/history called", companyId);
        var res = new GenericResponse<List<GiftCardTransactionDto>>();
        try {
            var transactions = transactionService.getCompanyTransactionHistory(companyId);
            res.data = transactions;
        } catch (Exception e) {
            logger.error("Error fetching transaction history for company {}: {}", companyId, e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

}
