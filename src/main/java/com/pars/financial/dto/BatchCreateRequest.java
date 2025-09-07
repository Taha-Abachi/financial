package com.pars.financial.dto;

import com.pars.financial.entity.Batch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;

public class BatchCreateRequest {
    @NotNull(message = "Batch type cannot be null")
    private Batch.BatchType batchType;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Total count cannot be null")
    @Min(value = 1, message = "Total count must be at least 1")
    @Max(value = 10000, message = "Total count cannot exceed 10000")
    private Integer totalCount;

    @NotNull(message = "Company ID cannot be null")
    private Long companyId;

    @NotNull(message = "Request user ID cannot be null")
    private Long requestUserId;

    // For discount code batches
    private List<DiscountCodeIssueRequest> discountCodeRequests;

    // For gift card batches
    private List<GiftCardIssueRequest> giftCardRequests;

    public BatchCreateRequest() {}

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

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getRequestUserId() {
        return requestUserId;
    }

    public void setRequestUserId(Long requestUserId) {
        this.requestUserId = requestUserId;
    }

    public List<DiscountCodeIssueRequest> getDiscountCodeRequests() {
        return discountCodeRequests;
    }

    public void setDiscountCodeRequests(List<DiscountCodeIssueRequest> discountCodeRequests) {
        this.discountCodeRequests = discountCodeRequests;
    }

    public List<GiftCardIssueRequest> getGiftCardRequests() {
        return giftCardRequests;
    }

    public void setGiftCardRequests(List<GiftCardIssueRequest> giftCardRequests) {
        this.giftCardRequests = giftCardRequests;
    }
} 