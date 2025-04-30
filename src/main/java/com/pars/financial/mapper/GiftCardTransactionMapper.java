package com.pars.financial.mapper;

import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.entity.GiftCardTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GiftCardTransactionMapper {
    public GiftCardTransactionDto getFrom(GiftCardTransaction transaction){
        if(transaction == null) return null;
        var dto = new GiftCardTransactionDto();
        dto.transactionId = transaction.getTransactionId();
        dto.phoneNo = transaction.getCustomer() == null ? "" : transaction.getCustomer().getPrimaryPhoneNumber();
        dto.giftCardSerialNo = transaction.getGiftCard().getSerialNo();
        dto.requestId = transaction.getDebitTransaction() == null ? null : transaction.getDebitTransaction().getTransactionId();
        dto.trxType = transaction.getTransactionType();
        dto.trxDate = transaction.getTrxDate();
        dto.amount = transaction.getAmount();
        dto.clientTransactionId = transaction.getClientTransactionId();
        dto.storeName = transaction.getStore().getStore_name();
        dto.storeId = transaction.getStore().getId();
        return dto;
    }

    public List<GiftCardTransactionDto> getFrom(List<GiftCardTransaction> transactions){
        if(transactions == null) return null;
        List<GiftCardTransactionDto> dtos = new ArrayList<>();
        for(GiftCardTransaction transaction : transactions) {
            var dto = getFrom(transaction);
            dtos.add(dto);
        }
        return dtos;
    }
}
