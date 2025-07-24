package com.pars.financial.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.pars.financial.enums.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
    @Index(name = "idx_discount_code", columnList = "code"),
    @Index(name = "idx_discount_serial", columnList = "serialNo")
})
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(20)")
    private String code;
    private Long serialNo;
    private int percentage;
    private long maxDiscountAmount = Long.MAX_VALUE;
    private long minimumBillAmount = 0;
    private long constantDiscountAmount = 0;
    private int usageLimit = 1;
    private int currentUsageCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) NOT NULL DEFAULT 'PERCENTAGE'")
    private DiscountType discountType = DiscountType.PERCENTAGE;

    private LocalDateTime issueDate;
    private LocalDate expiryDate;
    private LocalDateTime redeemDate;

    @Column(columnDefinition = "boolean default false")
    private boolean used = false;
    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(columnDefinition = "boolean default false")
    private boolean storeLimited = false;

    @ManyToMany
    @JoinTable(
        name = "discountcode_store_limitation",
        joinColumns = @JoinColumn(name = "discountcode_id"),
        inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> allowedStores;

    @OneToMany(mappedBy = "discountCode")
    private List<DiscountCodeTransaction> transactions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(Long serialNo) {
        this.serialNo = serialNo;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public long getMaxDiscountAmount() {
        return maxDiscountAmount == 0 ? Long.MAX_VALUE : maxDiscountAmount;
    }

    public void setMaxDiscountAmount(long maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount == 0 ? Long.MAX_VALUE : maxDiscountAmount;
    }

    public long getMinimumBillAmount() {
        return minimumBillAmount;
    }

    public void setMinimumBillAmount(long minimumBillAmount) {
        this.minimumBillAmount = minimumBillAmount;
    }

    public long getConstantDiscountAmount() {
        return constantDiscountAmount;
    }

    public void setConstantDiscountAmount(long constantDiscountAmount) {
        this.constantDiscountAmount = constantDiscountAmount;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getCurrentUsageCount() {
        return currentUsageCount;
    }

    public void setCurrentUsageCount(int currentUsageCount) {
        this.currentUsageCount = currentUsageCount;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getRedeemDate() {
        return redeemDate;
    }

    public void setRedeemDate(LocalDateTime redeemDate) {
        this.redeemDate = redeemDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public boolean isStoreLimited() {
        return storeLimited;
    }

    public void setStoreLimited(boolean storeLimited) {
        this.storeLimited = storeLimited;
    }

    public Set<Store> getAllowedStores() {
        return allowedStores;
    }

    public void setAllowedStores(Set<Store> allowedStores) {
        this.allowedStores = allowedStores;
    }

    public List<DiscountCodeTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<DiscountCodeTransaction> transactions) {
        this.transactions = transactions;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
