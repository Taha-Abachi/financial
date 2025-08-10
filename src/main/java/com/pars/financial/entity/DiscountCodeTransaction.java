package com.pars.financial.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_dctransactionId", // Name of the constraint
                        columnNames = {"transactionId", "trxType"} // Columns to include in the unique constraint
                ),
                @UniqueConstraint(
                        name = "unique_dcclientTransactionId", // Name of the constraint
                        columnNames = {"clientTransactionId","trxType", "api_user_id"} // Columns to include in the unique constraint
                )
        }
)
public class DiscountCodeTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime trxDate;

    @ManyToOne
    @JoinColumn(name = "discount_code_id")
    private DiscountCode discountCode;

    @ManyToOne
    @JoinColumn(name = "api_user_id")
    private User apiUser;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "redeem_transaction_id")
    private DiscountCodeTransaction redeemTransaction;

    @Enumerated(EnumType.STRING)
    private TransactionType trxType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private long originalAmount;
    private long discountAmount;
    private UUID transactionId = UUID.randomUUID();
    private String clientTransactionId;

    @Column(columnDefinition = "varchar(25)")
    private String orderno;

    @Column(columnDefinition = "varchar(250)")
    private String description;

    public DiscountCodeTransaction getRedeemTransaction() {
        return redeemTransaction;
    }

    public void setRedeemTransaction(DiscountCodeTransaction redeemTransaction) {
        this.redeemTransaction = redeemTransaction;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public long getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(long originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getTrxDate() {
        return trxDate;
    }

    public void setTrxDate(LocalDateTime trxDate) {
        this.trxDate = trxDate;
    }

    public TransactionType getTrxType() {
        return trxType;
    }

    public void setTrxType(TransactionType trxType) {
        this.trxType = trxType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getApiUser() {
        return apiUser;
    }

    public void setApiUser(User apiUser) {
        this.apiUser = apiUser;
    }

    public DiscountCode getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(DiscountCode discountCode) {
        this.discountCode = discountCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
