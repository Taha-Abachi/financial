package com.pars.financial.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.service.DiscountCodeTransactionService;
import com.pars.financial.utils.ApiUserUtil;

@RestController
@RequestMapping("/api/v1/discountcode/transaction")
public class DiscountCodeTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeTransactionController.class);

    private final DiscountCodeTransactionService discountCodeTransactionService;

    public DiscountCodeTransactionController(DiscountCodeTransactionService discountCodeTransactionService) {
        this.discountCodeTransactionService = discountCodeTransactionService;
    }

    @PostMapping("/redeem")
    public GenericResponse<DiscountCodeTransactionDto> redeem(@RequestBody DiscountCodeTransactionDto transactionDto) {
        logger.info("POST /api/v1/discountcode/transaction/redeem called with request: {}", transactionDto);
        GenericResponse<DiscountCodeTransactionDto> response = new GenericResponse<>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = -1;
            return response;
        }
        var dto = discountCodeTransactionService.redeem(apiUser, transactionDto);
        if(dto == null){
            logger.warn("Discount code redeem failed for transaction: {}", transactionDto);
            response.status = -1;
            response.message = "Discount code redeem filed";
        }
        response.data = dto;
        return response;
    }

    @PostMapping("/confirm")
    public GenericResponse<DiscountCodeTransactionDto> confirm(@RequestBody DiscountCodeTransactionDto transactionDto) {
        logger.info("POST /api/v1/discountcode/transaction/confirm called with request: {}", transactionDto);
        GenericResponse<DiscountCodeTransactionDto> response = new GenericResponse<>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = -1;
            return response;
        }
        var dto = discountCodeTransactionService.confirm(apiUser, transactionDto);
        if(dto == null){
            logger.warn("Discount Transaction confirmation failed for transaction: {}", transactionDto);
            response.status = -1;
            response.message = "Discount Transaction confirmation failed";
        }
        response.data = dto;
        return response;
    }

    @PostMapping("/reverse")
    public GenericResponse<DiscountCodeTransactionDto> reverse(@RequestBody DiscountCodeTransactionDto transactionDto) {
        logger.info("POST /api/v1/discountcode/transaction/reverse called with request: {}", transactionDto);
        GenericResponse<DiscountCodeTransactionDto> response = new GenericResponse<>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = -1;
            return response;
        }
        var dto = discountCodeTransactionService.reverse(apiUser, transactionDto);
        if(dto == null){
            logger.warn("Discount Transaction reverse failed for transaction: {}", transactionDto);
            response.status = -1;
            response.message = "Discount Transaction reverse failed.";
        }
        response.data = dto;
        return response;
    }

    @PostMapping("/refund")
    public GenericResponse<DiscountCodeTransactionDto> refund(@RequestBody DiscountCodeTransactionDto transactionDto) {
        logger.info("POST /api/v1/discountcode/transaction/refund called with request: {}", transactionDto);
        GenericResponse<DiscountCodeTransactionDto> response = new GenericResponse<>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = -1;
            return response;
        }
        var dto = discountCodeTransactionService.refund(apiUser, transactionDto);
        if(dto == null){
            logger.warn("Discount Transaction refund failed for transaction: {}", transactionDto);
            response.status = -1;
            response.message = "Discount Transaction refund failed.";
        }
        response.data = dto;
        return response;
    }

    @GetMapping("/followup/{transactionId}")
    public GenericResponse<DiscountCodeTransactionDto> get(@PathVariable UUID transactionId) {
        logger.info("GET /api/v1/discountcode/transaction/followup/{} called", transactionId);
        GenericResponse<DiscountCodeTransactionDto> response = new GenericResponse<>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = -1;
            return response;
        }
        var dto = discountCodeTransactionService.getTransaction(transactionId);
        if(dto == null){
            logger.warn("Discount Code transaction not found for transactionId: {}", transactionId);
            response.status = -1;
            response.message = "Discount Code transaction not found";
        }
        response.data = dto;
        return response;
    }
}
