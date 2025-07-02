package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import org.springframework.stereotype.Component;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;

@Component
public class GiftCardMapper {

    private final GiftCardTransactionMapper giftCardTransactionMapper;

    public GiftCardMapper(GiftCardTransactionMapper giftCardTransactionMapper) {
        this.giftCardTransactionMapper = giftCardTransactionMapper;
    }


    public GiftCardDto getFrom(GiftCard gc) {
        if(gc == null) {
            return null;
        }
        GiftCardDto dto = new GiftCardDto();
        dto.serialNo = gc.getSerialNo();
        dto.balance = gc.getBalance();
        var valDays = gc.getExpiryDate().toEpochDay() - gc.getIssueDate().toEpochDay();
        dto.remainingValidityPeriod = valDays <=0 ? 0 : valDays;
        dto.expiryDate = gc.getExpiryDate();
        dto.realAmount = gc.getRealAmount();
        dto.isActive = gc.isActive();
        dto.isBlocked = gc.isBlocked();
        dto.isAnonymous = (gc.getCustomer() == null);
        dto.identifier = gc.getIdentifier();
        var company = gc.getCompany();
        if(company != null) {
            dto.companyId = gc.getCompany().getId();
        }
        dto.transactions = new ArrayList<>();
        var debits = gc.getTransactions().stream().filter(p->p.getTransactionType() == TransactionType.Debit).toList();
        for(GiftCardTransaction transaction : debits) {
            var ldto = giftCardTransactionMapper.getFrom(transaction);
//            ldto.status = TransactionStatus.Pending;
//            ldto.status =
//                    gc.getTransactions().stream().anyMatch(
//                            p-> (
//                                    (
//                                        p.getTransactionType() == TransactionType.Refund)
//                                        && (p.getDebitTransaction().getTransactionId() == transaction.getTransactionId())
//                                    )
//                                )
//                    ? TransactionStatus.Refunded :
//                            gc.getTransactions().stream().anyMatch(
//                                    p-> (
//                                            (
//                                                    p.getTransactionType() == TransactionType.Reversal)
//                                                    && (p.getDebitTransaction().getTransactionId() == transaction.getTransactionId())
//                                    )
//                            )
//                            ? TransactionStatus.Reversed :
//                                    gc.getTransactions().stream().anyMatch(
//                                            p-> (
//                                                    (
//                                                            p.getTransactionType() == TransactionType.Confirmation)                                                            && (p.getDebitTransaction().getTransactionId() == transaction.getTransactionId())
//                                            )
//                                    )
//                            ? TransactionStatus.Confirmed :
//                            TransactionStatus.Pending;
            dto.transactions.add(ldto);
        }
        return dto;
    }

    public List<GiftCardDto> getFrom(List<GiftCard> gcList) {
        List<GiftCardDto> dtos = new ArrayList<>();
        for (var i = 0; i < gcList.size(); i++) {
            dtos.add(getFrom(gcList.get(i)));
        }
        return dtos;
    }
}
