package com.pars.financial.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DiscountCodeDto {
    public String code;
    public Long serialNo;
    public int percentage;
    public long maxDiscountAmount;
    public LocalDateTime issueDate;
    public long remainingValidityPeriod;
    public boolean used;
    public boolean active;
    public LocalDateTime redeemDate;
}
