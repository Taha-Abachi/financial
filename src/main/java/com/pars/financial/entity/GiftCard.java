package com.pars.financial.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(indexes = {
    @Index(name = "idx_giftcard_serial", columnList = "serialNo"),
    @Index(name = "idx_giftcard_identifier", columnList = "identifier")
})
public class GiftCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(20)")
    private String serialNo;
    private Long identifier;

    @NotNull(message = "Real amount cannot be null")
    @Min(value = 1, message = "Real amount must be greater than 0")
    private Long realAmount;
    private long initialAmount;
    private long balance;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private LocalDateTime lastUsed;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;
    @Column(columnDefinition = "boolean default false")
    private boolean blocked = false;

    @Column(columnDefinition = "boolean default false")
    private boolean storeLimited = false;

    @OneToMany(mappedBy = "giftCard")
    private List<GiftCardTransaction> transactions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "giftcard_store_limitation",
        joinColumns = @JoinColumn(name = "giftcard_id"),
        inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> allowedStores;

    @Column(columnDefinition = "boolean default false")
    private boolean itemCategoryLimited = false;

    @ManyToMany
    @JoinTable(
        name = "giftcard_item_category_limitation",
        joinColumns = @JoinColumn(name = "giftcard_id"),
        inverseJoinColumns = @JoinColumn(name = "item_category_id")
    )
    private Set<ItemCategory> allowedItemCategories;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(Long realAmount) {
        this.realAmount = realAmount;
    }


    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public long getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(long initialAmount) {
        this.initialAmount = initialAmount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
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

    public boolean isItemCategoryLimited() {
        return itemCategoryLimited;
    }

    public void setItemCategoryLimited(boolean itemCategoryLimited) {
        this.itemCategoryLimited = itemCategoryLimited;
    }

    public Set<ItemCategory> getAllowedItemCategories() {
        return allowedItemCategories;
    }

    public void setAllowedItemCategories(Set<ItemCategory> allowedItemCategories) {
        this.allowedItemCategories = allowedItemCategories;
    }

    public List<GiftCardTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<GiftCardTransaction> transactions) {
        this.transactions = transactions;
    }
}
