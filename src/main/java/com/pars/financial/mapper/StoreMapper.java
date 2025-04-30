package com.pars.financial.mapper;

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
        c.address = store.getAddress().getText();
        c.phone = store.getPhone_number().getNumber();
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
