package com.pars.financial.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.StoreDto;
import com.pars.financial.entity.Store;
import com.pars.financial.mapper.StoreMapper;
import com.pars.financial.repository.StoreRepository;

@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }

    @Cacheable(value = "stores", key = "#id")
    @Transactional(readOnly = true)
    public StoreDto getStore(Long id) {
        logger.debug("Fetching store with ID: {}", id);
        Optional<Store> st = storeRepository.findById(id);
        if (!st.isPresent()) {
            logger.warn("Store not found with ID: {}", id);
            return null;
        }
        return storeMapper.getFrom(st.get());
    }

    @Transactional(readOnly = true)
    public List<StoreDto> getAllStores() {
        logger.debug("Fetching all stores");
        List<Store> stores = storeRepository.findAll();
        logger.debug("Found {} stores", stores.size());
        return storeMapper.getFrom(stores);
    }
}
