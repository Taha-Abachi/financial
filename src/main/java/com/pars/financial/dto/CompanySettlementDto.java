package com.pars.financial.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Company-level settlement summary
 */
public class CompanySettlementDto {
    private Long companyId;
    private String companyName;
    
    // Payable: Money this company owes to others
    private BigDecimal totalPayable;
    
    // Receivable: Money this company should receive from others
    private BigDecimal totalReceivable;
    
    // Net: Receivable - Payable (positive = net receivable, negative = net payable)
    private BigDecimal netAmount;
    
    // Store-level breakdown
    private List<StoreSettlementDto> stores;

    public CompanySettlementDto() {
        this.totalPayable = BigDecimal.ZERO;
        this.totalReceivable = BigDecimal.ZERO;
        this.netAmount = BigDecimal.ZERO;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public BigDecimal getTotalReceivable() {
        return totalReceivable;
    }

    public void setTotalReceivable(BigDecimal totalReceivable) {
        this.totalReceivable = totalReceivable;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public List<StoreSettlementDto> getStores() {
        return stores;
    }

    public void setStores(List<StoreSettlementDto> stores) {
        this.stores = stores;
    }
}

