package com.pars.financial.dto;

import java.time.LocalDateTime;

import com.pars.financial.entity.Batch;

public class BatchDto {
    private Long id;
    private String batchNumber;
    private Batch.BatchType batchType;
    private String description;
    private LocalDateTime requestDate;
    private Integer totalCount;
    private Batch.BatchStatus status;
    private Integer processedCount;
    private Integer failedCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto requestUser;
    private CompanyDto company;

    public BatchDto() {}

    public BatchDto(Long id, String batchNumber, Batch.BatchType batchType, String description, LocalDateTime requestDate, Integer totalCount, Batch.BatchStatus status, Integer processedCount, Integer failedCount, String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt, UserDto requestUser, CompanyDto company) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.batchType = batchType;
        this.description = description;
        this.requestDate = requestDate;
        this.totalCount = totalCount;
        this.status = status;
        this.processedCount = processedCount;
        this.failedCount = failedCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.requestUser = requestUser;
        this.company = company;
    }

    public static BatchDto fromEntity(Batch batch) {
        if (batch == null) {
            return null;
        }
        return new BatchDto(
            batch.getId(),
            batch.getBatchNumber(),
            batch.getBatchType(),
            batch.getDescription(),
            batch.getRequestDate(),
            batch.getTotalCount(),
            batch.getStatus(),
            batch.getProcessedCount(),
            batch.getFailedCount(),
            batch.getErrorMessage(),
            batch.getCreatedAt(),
            batch.getUpdatedAt(),
            UserDto.fromEntity(batch.getRequestUser()),
            CompanyDto.fromEntity(batch.getCompany())
        );
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

    public Batch.BatchType getBatchType() {
        return batchType;
    }

    public void setBatchType(Batch.BatchType batchType) {
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

    public Batch.BatchStatus getStatus() {
        return status;
    }

    public void setStatus(Batch.BatchStatus status) {
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

    public UserDto getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(UserDto requestUser) {
        this.requestUser = requestUser;
    }

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }
} 