package com.pars.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Master-level settlement report containing company summaries
 */
public class SettlementReportDto {
    private LocalDate reportDate;
    private LocalDateTime generatedAt;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long filterStoreId;
    private String filterStoreName;
    private Long filterCompanyId;
    private String filterCompanyName;
    
    // Summary totals
    private BigDecimal totalPayable;
    private BigDecimal totalReceivable;
    // Grand total (net settlement across all companies)
    private BigDecimal grandTotal;
    // Net settlement (same as grandTotal, for consistency)
    private BigDecimal netSettlement;
    
    // Company-level settlements
    private List<CompanySettlementDto> companies;
    
    // Transaction details
    private List<SettlementTransactionDetailDto> transactionDetails;

    public SettlementReportDto() {
        this.totalPayable = BigDecimal.ZERO;
        this.totalReceivable = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
        this.netSettlement = BigDecimal.ZERO;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Long getFilterStoreId() {
        return filterStoreId;
    }

    public void setFilterStoreId(Long filterStoreId) {
        this.filterStoreId = filterStoreId;
    }

    public String getFilterStoreName() {
        return filterStoreName;
    }

    public void setFilterStoreName(String filterStoreName) {
        this.filterStoreName = filterStoreName;
    }

    public Long getFilterCompanyId() {
        return filterCompanyId;
    }

    public void setFilterCompanyId(Long filterCompanyId) {
        this.filterCompanyId = filterCompanyId;
    }

    public String getFilterCompanyName() {
        return filterCompanyName;
    }

    public void setFilterCompanyName(String filterCompanyName) {
        this.filterCompanyName = filterCompanyName;
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

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public BigDecimal getNetSettlement() {
        return netSettlement;
    }

    public void setNetSettlement(BigDecimal netSettlement) {
        this.netSettlement = netSettlement;
    }

    public List<CompanySettlementDto> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanySettlementDto> companies) {
        this.companies = companies;
    }

    public List<SettlementTransactionDetailDto> getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(List<SettlementTransactionDetailDto> transactionDetails) {
        this.transactionDetails = transactionDetails;
    }
}

