package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

@Component
public class DiscountCodeTransactionMapper {
    public DiscountCodeTransactionDto getFrom(DiscountCodeTransaction transaction) {
        var cst = transaction.getCustomer();
        var dst = transaction.getDiscountCode();
        DiscountCodeTransactionDto dto = new DiscountCodeTransactionDto();
        dto.clientTransactionId = transaction.getClientTransactionId();
        dto.code = dst.getCode();
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

        // Set transaction status
        if (transaction.getTrxType() == TransactionType.Redeem) {
            // Check if there's a corresponding confirm or reverse transaction
            boolean hasConfirm = transaction.getDiscountCode().getTransactions().stream()
                .anyMatch(t -> t.getTrxType() == TransactionType.Confirmation 
                    && t.getRedeemTransaction() != null 
                    && t.getRedeemTransaction().getTransactionId().equals(transaction.getTransactionId()));
            
            boolean hasReverse = transaction.getDiscountCode().getTransactions().stream()
                .anyMatch(t -> t.getTrxType() == TransactionType.Reversal 
                    && t.getRedeemTransaction() != null 
                    && t.getRedeemTransaction().getTransactionId().equals(transaction.getTransactionId()));
            
            if (hasConfirm) {
                dto.status = TransactionStatus.Confirmed;
            } else if (hasReverse) {
                dto.status = TransactionStatus.Reversed;
            } else {
                dto.status = TransactionStatus.Pending;
            }
        } else {
            // For non-redeem transactions, status is not applicable
            dto.status = null;
        }

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
