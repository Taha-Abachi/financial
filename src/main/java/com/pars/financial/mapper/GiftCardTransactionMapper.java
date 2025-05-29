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
        
        // Set transaction status
        if (transaction.getTransactionType() == TransactionType.Debit) {
            var stream = transaction.getGiftCard().getTransactions().stream();
            // Check if there's a corresponding confirm or reverse transaction
            boolean hasConfirm = transaction.getGiftCard().getTransactions().stream()
                .anyMatch(t -> t.getTransactionType() == TransactionType.Confirmation 
                    && t.getDebitTransaction() != null 
                    && t.getDebitTransaction().getTransactionId().equals(transaction.getTransactionId()));
            
            boolean hasReverse = transaction.getGiftCard().getTransactions().stream()
                .anyMatch(t -> t.getTransactionType() == TransactionType.Reversal 
                    && t.getDebitTransaction() != null 
                    && t.getDebitTransaction().getTransactionId().equals(transaction.getTransactionId()));
            
            boolean hasRefund = transaction.getGiftCard().getTransactions().stream()
                .anyMatch(t -> t.getTransactionType() == TransactionType.Refund 
                    && t.getDebitTransaction() != null 
                    && t.getDebitTransaction().getTransactionId().equals(transaction.getTransactionId()));
            

            if (hasConfirm) {
                dto.status = TransactionStatus.Confirmed;
            } else if (hasReverse) {
                dto.status = TransactionStatus.Reversed;
                } else if (hasRefund) {
                dto.status = TransactionStatus.Refunded;
            } else {
                dto.status = TransactionStatus.Pending;
            }
        } else {
            // For non-debit transactions, status is not applicable
            dto.status = null;
        }
        
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
