package com.pars.financial.dto;

import com.pars.financial.enums.OwnershipType;
import com.pars.financial.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StoreCreateRequest {
    
    @NotBlank(message = "Store name is required")
    private String storeName;
    
    @NotNull(message = "Company ID is required")
    private Long companyId;
    
    @NotNull(message = "Ownership type is required")
    private OwnershipType ownershipType;
    
    @NotNull(message = "Location type is required")
    private LocationType locationType;
    
    private String phoneNumber;
    private String address;

    public StoreCreateRequest() {}

    public StoreCreateRequest(String storeName, Long companyId, OwnershipType ownershipType, LocationType locationType, String phoneNumber, String address) {
        this.storeName = storeName;
        this.companyId = companyId;
        this.ownershipType = ownershipType;
        this.locationType = locationType;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public OwnershipType getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(OwnershipType ownershipType) {
        this.ownershipType = ownershipType;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
