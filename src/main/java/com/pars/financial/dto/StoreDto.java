package com.pars.financial.dto;

import com.pars.financial.enums.OwnershipType;
import com.pars.financial.enums.LocationType;

public class StoreDto {
    public Long id;
    public String name;
    public String address;
    public String phone;
    public OwnershipType ownershipType;
    public LocationType locationType;
}
