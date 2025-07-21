package com.pars.financial.dto;

import com.pars.financial.enums.DiscountType;

public class DiscountCodeIssueRequest {
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
}
