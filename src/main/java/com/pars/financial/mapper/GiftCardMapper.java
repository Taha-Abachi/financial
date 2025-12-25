package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.ItemCategoryDto;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionType;

@Component
public class GiftCardMapper {

    private final GiftCardTransactionMapper giftCardTransactionMapper;
    private final StoreMapper storeMapper;

    public GiftCardMapper(GiftCardTransactionMapper giftCardTransactionMapper, StoreMapper storeMapper) {
        this.giftCardTransactionMapper = giftCardTransactionMapper;
        this.storeMapper = storeMapper;
    }


    public GiftCardDto getFrom(GiftCard gc) {
        if(gc == null) {
            return null;
        }
        GiftCardDto dto = new GiftCardDto();
        dto.serialNo = gc.getSerialNo();
        dto.title = gc.getTitle();
        dto.balance = gc.getBalance();
        var valDays = gc.getExpiryDate().toEpochDay() - gc.getIssueDate().toEpochDay();
        dto.remainingValidityPeriod = valDays <=0 ? 0 : valDays;
        dto.expiryDate = gc.getExpiryDate();
        dto.realAmount = gc.getRealAmount();
        dto.initialAmount = gc.getInitialAmount();
        dto.isActive = gc.isActive();
        dto.isBlocked = gc.isBlocked();
        dto.isAnonymous = (gc.getCustomer() == null);
        dto.identifier = gc.getIdentifier();
        var company = gc.getCompany();
        if(company != null) {
            dto.companyId = gc.getCompany().getId();
        }
        dto.storeLimited = gc.isStoreLimited();
        dto.itemCategoryLimited = gc.isItemCategoryLimited();
        
        // Map allowed stores
        if (gc.getAllowedStores() != null && !gc.getAllowedStores().isEmpty()) {
            dto.allowedStores = gc.getAllowedStores().stream()
                    .map(storeMapper::getFrom)
                    .collect(Collectors.toList());
        } else {
            dto.allowedStores = new ArrayList<>();
        }
        
        // Map allowed item categories
        if (gc.getAllowedItemCategories() != null && !gc.getAllowedItemCategories().isEmpty()) {
            dto.allowedItemCategories = gc.getAllowedItemCategories().stream()
                    .map(ItemCategoryDto::fromEntity)
                    .collect(Collectors.toList());
        } else {
            dto.allowedItemCategories = new ArrayList<>();
        }
        
        // Map batch information
        var batch = gc.getBatch();
        if (batch != null) {
            dto.batchId = batch.getId();
            dto.batchNumber = batch.getBatchNumber();
        }
        
        // Map type
        dto.type = gc.getType();
        
        // Map blocked information
        if (gc.getBlockedBy() != null) {
            dto.blockedByUserId = gc.getBlockedBy().getId();
            dto.blockedByUsername = gc.getBlockedBy().getUsername();
        }
        dto.blockedDate = gc.getBlockedDate();
        
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
