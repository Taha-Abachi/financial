package com.pars.financial.dto;

public class DiscountCodeIssueRequest {
    public int count;
    public int percentage;
    public long remainingValidityPeriod;
    public long maxDiscountAmount;
    public long minimumBillAmount;
}
