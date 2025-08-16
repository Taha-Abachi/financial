package com.pars.financial.dto;

import com.pars.financial.enums.DiscountType;

public class DiscountCodeValidationResponse {
    public boolean isValid;
    public String message;
    public String errorCode;
    public long calculatedDiscountAmount;
    public DiscountType discountType;
    public int percentage;
    public long maxDiscountAmount;
    public long minimumBillAmount;
    public int usageLimit;
    public int currentUsageCount;
    public long constantDiscountAmount;
    public boolean storeLimited;
    public boolean itemCategoryLimited;
}
