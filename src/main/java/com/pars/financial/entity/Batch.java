package com.pars.financial.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "batch")
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", nullable = false, unique = true)
    private String batchNumber;

    @Column(name = "batch_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BatchType batchType;

    @Column(name = "description")
    private String description;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BatchStatus status = BatchStatus.PENDING;

    @Column(name = "processed_count", nullable = false)
    private Integer processedCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_user_id", nullable = false)
    private User requestUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public enum BatchType {
        DISCOUNT_CODE,
        GIFT_CARD
    }

    public enum BatchStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public Batch() {}

    public Batch(String batchNumber, BatchType batchType, String description, Integer totalCount, User requestUser, Company company) {
        this.batchNumber = batchNumber;
        this.batchType = batchType;
        this.description = description;
        this.totalCount = totalCount;
        this.requestUser = requestUser;
        this.company = company;
        this.requestDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public BatchType getBatchType() {
        return batchType;
    }

    public void setBatchType(BatchType batchType) {
        this.batchType = batchType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public Integer getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(User requestUser) {
        this.requestUser = requestUser;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
} 