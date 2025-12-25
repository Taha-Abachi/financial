package com.pars.financial.dto;

import com.pars.financial.enums.DiscountType;
import com.pars.financial.enums.DiscountCodeType;

public class DiscountCodeIssueRequest {
    public String code;
    public Long serialNo;
    public String title;
    public int count;
    public int percentage;
    public long remainingValidityPeriod;
    public long maxDiscountAmount;
    public long minimumBillAmount;
    public long constantDiscountAmount;
    public DiscountType discountType = DiscountType.PERCENTAGE;
    public int usageLimit = 1;
    public Long companyId;
    public boolean storeLimited = false;
    public java.util.List<Long> allowedStoreIds;
    public boolean itemCategoryLimited = false;
    public java.util.List<Long> allowedItemCategoryIds;
    public DiscountCodeType type = DiscountCodeType.GENERAL;
    public String phoneNumber;
}
