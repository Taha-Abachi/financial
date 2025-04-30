package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.service.GiftCardTransactionService;
import com.pars.financial.utils.ApiUserUtil;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giftcard/transaction")
public class GiftCardTransactionController {
    final GiftCardTransactionService transactionService;

    public GiftCardTransactionController(GiftCardTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/debit")
    public GenericResponse<GiftCardTransactionDto> debit(@RequestBody GiftCardTransactionDto dto) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        var trx = transactionService.debitGiftCard(apiUser, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.storeId, dto.phoneNo);
        if (trx == null) {
            res.message = "Failed to debit gift card";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @PostMapping("/reverse")
    public GenericResponse<GiftCardTransactionDto> reverse(@RequestBody GiftCardTransactionDto dto) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }
        var trx = transactionService.reverseTransaction(apiUser, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId);
        if (trx == null) {
            res.message = "Failed to reverse gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @PostMapping("/confirm")
    public GenericResponse<GiftCardTransactionDto> confirm(@RequestBody GiftCardTransactionDto dto) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }
        var trx = transactionService.confirmTransaction(apiUser, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId);
        if (trx == null) {
            res.message = "Failed to confirm gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @PostMapping("/refund")
    public GenericResponse<GiftCardTransactionDto> refund(@RequestBody GiftCardTransactionDto dto) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }
        var trx = transactionService.refundTransaction(apiUser, dto.clientTransactionId, dto.amount, dto.giftCardSerialNo, dto.transactionId);
        if (trx == null) {
            res.message = "Failed to refund gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/checkStatus/{clientTransactionId}")
    public GenericResponse<GiftCardTransactionDto> checkTransactionStatus(@PathVariable String clientTransactionId) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        var trx = transactionService.checkStatus(clientTransactionId);
        if (trx == null) {
            res.message = "Failed to find gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/{transactionId}")
    public GenericResponse<GiftCardTransactionDto> getTransaction(@PathVariable String transactionId) {
        var res = new GenericResponse<GiftCardTransactionDto>();
        var trx = transactionService.get(transactionId);
        if (trx == null) {
            res.message = "Failed to find gift card transaction.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

    @GetMapping("/list/{serialNo}")
    public GenericResponse<List<GiftCardTransactionDto>> getTransactionHistory(@PathVariable String serialNo) {
        var res = new GenericResponse<List<GiftCardTransactionDto>>();
        var trx = transactionService.getTransactionHistory(serialNo);
        if (trx == null) {
            res.message = "Failed to find gift card.";
            res.status = -1;
        }
        assert trx != null;
        res.data = trx;
        return res;
    }

}
