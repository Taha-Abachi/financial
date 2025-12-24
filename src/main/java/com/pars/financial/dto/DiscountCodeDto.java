package com.pars.financial.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.pars.financial.enums.DiscountType;
import com.pars.financial.enums.DiscountCodeType;

public class DiscountCodeDto {
    public String code;
    public Long serialNo;
    public int percentage;
    public long maxDiscountAmount;
    public long minimumBillAmount;
    public long constantDiscountAmount;
    public DiscountType discountType;
    public int usageLimit;
    public int currentUsageCount;
    public LocalDateTime issueDate;
    public LocalDate expireDate;
    public long remainingValidityPeriod;
    public boolean used;
    public boolean active;
    public boolean isUsable;
    public boolean blocked;
    public Long blockedByUserId;
    public String blockedByUsername;
    public LocalDateTime blockedDate;
    public LocalDateTime redeemDate;
    public ArrayList<DiscountCodeTransactionDto> transactions;
    public Long companyId;
    public boolean storeLimited;
    public boolean itemCategoryLimited;
    public java.util.List<StoreDto> allowedStores;
    public java.util.List<ItemCategoryDto> allowedItemCategories;
    public Long batchId;
    public String batchNumber;
    public DiscountCodeType type;
    public Long customerId;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
}
