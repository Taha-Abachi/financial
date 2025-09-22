package com.pars.financial.dto;

import java.util.List;

public class TransactionAggregationResponseDto {
    public List<TransactionAggregationDto> today;
    public List<TransactionAggregationDto> last7Days;
    public List<TransactionAggregationDto> last30Days;
    
    public TransactionAggregationResponseDto() {}
    
    public TransactionAggregationResponseDto(List<TransactionAggregationDto> today, 
                                           List<TransactionAggregationDto> last7Days, 
                                           List<TransactionAggregationDto> last30Days) {
        this.today = today;
        this.last7Days = last7Days;
        this.last30Days = last30Days;
    }
}
