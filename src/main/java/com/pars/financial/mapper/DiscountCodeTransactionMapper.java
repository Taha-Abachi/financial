package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.entity.DiscountCodeTransaction;

@Component
public class DiscountCodeTransactionMapper {
    public DiscountCodeTransactionDto getFrom(DiscountCodeTransaction transaction) {
        var cst = transaction.getCustomer();
        var dst = transaction.getDiscountCode();
        DiscountCodeTransactionDto dto = new DiscountCodeTransactionDto();
        dto.clientTransactionId = transaction.getClientTransactionId();
        dto.code = dst.getCode();
        dto.discountType = dst.getDiscountType();
        dto.originalAmount = transaction.getOriginalAmount();
        dto.transactionId = transaction.getTransactionId();
        dto.maxDiscountAmount = dst.getMaxDiscountAmount();
        dto.discountAmount = transaction.getDiscountAmount();
        dto.storeId = transaction.getStore().getId();
        dto.maxDiscountAmount = dst.getMaxDiscountAmount();
        dto.phoneNo = cst == null ? "" : cst.getPrimaryPhoneNumber();
        dto.percentage = dst.getPercentage();
        dto.trxType = transaction.getTrxType();
        dto.trxDate = transaction.getTrxDate();
        dto.storeName = transaction.getStore().getStore_name();
        dto.orderno = transaction.getOrderno();
        dto.description = transaction.getDescription();

        // Set transaction status
        dto.status = transaction.getStatus();

        return dto;
    }

    public List<DiscountCodeTransactionDto> getFrom(List<DiscountCodeTransaction> transaction) {
        List<DiscountCodeTransactionDto> dtos = new ArrayList<>();
        for (DiscountCodeTransaction transactionDto : transaction) {
            dtos.add(getFrom(transactionDto));
        }
        return dtos;
    }
}
