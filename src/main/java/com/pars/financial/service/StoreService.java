package com.pars.financial.service;

import java.util.Optional;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.StoreDto;
import com.pars.financial.entity.Store;
import com.pars.financial.mapper.StoreMapper;
import com.pars.financial.repository.StoreRepository;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }

    @Cacheable(value = "stores", key = "#id")
    @Transactional(readOnly = true)
    public StoreDto getStore(Long id) {
        Optional<Store> st = storeRepository.findById(id);
        return storeMapper.getFrom(st.get());
    }

    @Transactional(readOnly = true)
    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return storeMapper.getFrom(stores);
    }
}
