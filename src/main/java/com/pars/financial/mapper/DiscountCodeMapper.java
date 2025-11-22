package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.enums.TransactionType;
import org.springframework.stereotype.Component;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.entity.DiscountCode;

@Component
public class DiscountCodeMapper {
    private final DiscountCodeTransactionMapper discountCodeTransactionMapper;

    public DiscountCodeMapper(DiscountCodeTransactionMapper discountCodeTransactionMapper) {
        this.discountCodeTransactionMapper = discountCodeTransactionMapper;
    }

    public DiscountCodeDto getFrom(DiscountCode code) {
        if(code == null) {
            return null;
        }
        DiscountCodeDto dto = new DiscountCodeDto();
        dto.code = code.getCode();
        dto.serialNo = code.getSerialNo();
        var valDays = code.getExpiryDate().toEpochDay() - code.getIssueDate().toLocalDate().toEpochDay();
        dto.remainingValidityPeriod = valDays >= 0 ? valDays : 0;
        dto.maxDiscountAmount = code.getMaxDiscountAmount();
        dto.minimumBillAmount = code.getMinimumBillAmount();
        dto.constantDiscountAmount = code.getConstantDiscountAmount();
        dto.discountType = code.getDiscountType();
        dto.usageLimit = code.getUsageLimit();
        dto.currentUsageCount = code.getCurrentUsageCount();
        dto.percentage = code.getPercentage();
        dto.used = code.isUsed();
        dto.active = code.isActive();
        dto.blocked = code.isBlocked();
        dto.isUsable = code.calculateIsUsable();
        dto.issueDate = code.getIssueDate();
        dto.expireDate = code.getExpiryDate();
        dto.redeemDate = code.getRedeemDate();
        
        // Map blocked information
        if (code.getBlockedBy() != null) {
            dto.blockedByUserId = code.getBlockedBy().getId();
            dto.blockedByUsername = code.getBlockedBy().getUsername();
        }
        dto.blockedDate = code.getBlockedDate();
        
        dto.companyId = code.getCompany() != null ? code.getCompany().getId() : null;
        dto.storeLimited = code.isStoreLimited();
        dto.itemCategoryLimited = code.isItemCategoryLimited();
        
        // Map batch information
        var batch = code.getBatch();
        if (batch != null) {
            dto.batchId = batch.getId();
            dto.batchNumber = batch.getBatchNumber();
        }
        ArrayList<DiscountCodeTransactionDto> transactions = new ArrayList<>();
        code.getTransactions().stream().filter(t->t.getTrxType() == TransactionType.Redeem).forEach(p->
        {
            transactions.add(discountCodeTransactionMapper.getFrom(p));
        });
        dto.transactions = transactions;
        return dto;
    }

    public List<DiscountCodeDto> getFrom(List<DiscountCode> codes) {
        if(codes == null) {
            return null;
        }
        List<DiscountCodeDto> dtos = new ArrayList<>();
        for (DiscountCode code : codes) {
            dtos.add(getFrom(code));
        }
        return dtos;
    }
}
