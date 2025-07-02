package com.pars.financial.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.pars.financial.enums.DiscountType;

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
    public long remainingValidityPeriod;
    public boolean used;
    public boolean active;
    public LocalDateTime redeemDate;
    public ArrayList<DiscountCodeTransactionDto> transactions;
    public Long companyId;
}
