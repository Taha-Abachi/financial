package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

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
        dto.status = transaction.getStatus();
        // Set transaction status
//        if (transaction.getTransactionType() == TransactionType.Debit) {
//            // Ensure we have initialized transactions
//            if (transaction.getGiftCard() == null || transaction.getGiftCard().getTransactions() == null) {
//                dto.status = TransactionStatus.Unkown;
//
//            }
//            else{
//                dto.status = TransactionStatus.Pending;
//                var stream = transaction.getGiftCard().getTransactions().stream().filter(p->p.getTransactionId().equals(transaction.getTransactionId()));
//                dto.status = stream.anyMatch(p -> (p.getTransactionType() == TransactionType.Refund))
//                        ? TransactionStatus.Refunded
//                        : stream.anyMatch(p -> (p.getTransactionType() == TransactionType.Reversal))
//                        ? TransactionStatus.Reversed
//                        : stream.anyMatch(p -> (p.getTransactionType() == TransactionType.Confirmation))
//                        ? TransactionStatus.Confirmed
//                        : TransactionStatus.Pending;
//                return dto;
//            }
//                dto.status = TransactionStatus.Unkown;
//        } else {
//            // For non-debit transactions, status is not applicable
//            dto.status = null;
//        }
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
