package com.pars.financial.dto;

import com.pars.financial.enums.OwnershipType;
import com.pars.financial.enums.LocationType;

public class StoreUpdateRequest {
    
    private String storeName;
    private Long companyId;
    private OwnershipType ownershipType;
    private LocationType locationType;
    private String phoneNumber;
    private String address;
    private Boolean isActive;

    public StoreUpdateRequest() {}

    public StoreUpdateRequest(String storeName, Long companyId, OwnershipType ownershipType, LocationType locationType, String phoneNumber, String address, Boolean isActive) {
        this.storeName = storeName;
        this.companyId = companyId;
        this.ownershipType = ownershipType;
        this.locationType = locationType;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isActive = isActive;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
