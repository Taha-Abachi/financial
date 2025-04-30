package com.pars.financial.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pars.financial.enums.TransactionType;

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
                        name = "unique_transactionId", // Name of the constraint
                        columnNames = {"transactionId", "transactionType"} // Columns to include in the unique constraint
                ),
                @UniqueConstraint(
                        name = "unique_clientTransactionId", // Name of the constraint
                        columnNames = {"clientTransactionId","transactionType", "api_user_id"} // Columns to include in the unique constraint
                )
        }
)
public class GiftCardTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private UUID transactionId = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "gift_card_id")
    GiftCard giftCard;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "api_user_id")
    private ApiUser apiUser;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private LocalDateTime trxDate = LocalDateTime.now();
    private long Amount;
    private long balanceBefore;

    private String clientTransactionId;

    @ManyToOne
    @JoinColumn(name = "debit_transaction_id")
    private GiftCardTransaction debitTransaction;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public GiftCardTransaction getDebitTransaction() {
        return debitTransaction;
    }

    public void setDebitTransaction(GiftCardTransaction debitTransaction) {
        this.debitTransaction = debitTransaction;
    }
    public ApiUser getApiUser() {
        return apiUser;
    }

    public void setApiUser(ApiUser apiUser) {
        this.apiUser = apiUser;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTrxDate() {
        return trxDate;
    }

    public void setTrxDate(LocalDateTime trxDate) {
        this.trxDate = trxDate;
    }

    public long getAmount() {
        return Amount;
    }

    public void setAmount(long amount) {
        Amount = amount;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public long getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(long balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
}


