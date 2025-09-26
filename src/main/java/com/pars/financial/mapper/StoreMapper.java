package com.pars.financial.mapper;

import com.pars.financial.dto.SimpleCompanyDto;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.entity.Store;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StoreMapper {
    public StoreDto getFrom(Store store) {
        StoreDto c = new StoreDto();
        c.id = store.getId();
        c.name = store.getStore_name();
        
        // Safe null handling for address
        if (store.getAddress() != null) {
            c.address = store.getAddress().getText();
        } else {
            c.address = null;
        }
        
        // Safe null handling for phone number
        if (store.getPhone_number() != null) {
            c.phone = store.getPhone_number().getNumber();
        } else {
            c.phone = null;
        }
        
        c.ownershipType = store.getOwnershipType();
        c.locationType = store.getLocationType();
        
        // Safe null handling for company - this is the main issue
        if (store.getCompany() != null) {
            c.company = SimpleCompanyDto.fromEntity(store.getCompany());
        } else {
            c.company = null;
        }
        
        return c;
    }
    public List<StoreDto> getFrom(List<Store> stores) {
        if(stores == null) return null;
        List<StoreDto> dtos = new ArrayList<>();
        for(Store str : stores) {
            dtos.add(getFrom(str));
        }
        return dtos;
    }
}
