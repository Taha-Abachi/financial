package com.pars.financial.dto;

import com.pars.financial.enums.TransactionStatus;

public class TransactionAggregationDto {
    public TransactionStatus status;
    public long count;
    public long totalAmount;
    public long totalOrderAmount;
    
    public TransactionAggregationDto() {}
    
    public TransactionAggregationDto(TransactionStatus status, long count, long totalAmount, long totalOrderAmount) {
        this.status = status;
        this.count = count;
        this.totalAmount = totalAmount;
        this.totalOrderAmount = totalOrderAmount;
    }
}
