package com.pars.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Detailed transaction information for settlement report
 */
public class SettlementTransactionDetailDto {
    private Long transactionId;
    private UUID transactionUuid;
    private LocalDateTime transactionDate;
    
    // Gift card info
    private Long giftCardId;
    private String giftCardSerialNo;
    
    // Issuer (who created the gift card)
    private Long issuerCompanyId;
    private String issuerCompanyName;
    
    // Usage location (where the gift card was used)
    private Long usageStoreId;
    private String usageStoreName;
    private Long usageStoreCompanyId;
    private String usageStoreCompanyName;
    
    // Transaction amounts
    private BigDecimal transactionAmount;
    private BigDecimal orderAmount;
    
    // Settlement type
    private String settlementType; // "PAYABLE" or "RECEIVABLE"
    
    // Customer info
    private Long customerId;
    private String customerName;
    private String customerPhoneNumber;
    
    private String orderNumber;
    private String description;

    public SettlementTransactionDetailDto() {
        this.transactionAmount = BigDecimal.ZERO;
        this.orderAmount = BigDecimal.ZERO;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getTransactionUuid() {
        return transactionUuid;
    }

    public void setTransactionUuid(UUID transactionUuid) {
        this.transactionUuid = transactionUuid;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getGiftCardId() {
        return giftCardId;
    }

    public void setGiftCardId(Long giftCardId) {
        this.giftCardId = giftCardId;
    }

    public String getGiftCardSerialNo() {
        return giftCardSerialNo;
    }

    public void setGiftCardSerialNo(String giftCardSerialNo) {
        this.giftCardSerialNo = giftCardSerialNo;
    }

    public Long getIssuerCompanyId() {
        return issuerCompanyId;
    }

    public void setIssuerCompanyId(Long issuerCompanyId) {
        this.issuerCompanyId = issuerCompanyId;
    }

    public String getIssuerCompanyName() {
        return issuerCompanyName;
    }

    public void setIssuerCompanyName(String issuerCompanyName) {
        this.issuerCompanyName = issuerCompanyName;
    }

    public Long getUsageStoreId() {
        return usageStoreId;
    }

    public void setUsageStoreId(Long usageStoreId) {
        this.usageStoreId = usageStoreId;
    }

    public String getUsageStoreName() {
        return usageStoreName;
    }

    public void setUsageStoreName(String usageStoreName) {
        this.usageStoreName = usageStoreName;
    }

    public Long getUsageStoreCompanyId() {
        return usageStoreCompanyId;
    }

    public void setUsageStoreCompanyId(Long usageStoreCompanyId) {
        this.usageStoreCompanyId = usageStoreCompanyId;
    }

    public String getUsageStoreCompanyName() {
        return usageStoreCompanyName;
    }

    public void setUsageStoreCompanyName(String usageStoreCompanyName) {
        this.usageStoreCompanyName = usageStoreCompanyName;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

