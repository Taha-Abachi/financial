package com.pars.financial.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.PagedResponse;
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
        Store store = storeRepository.findByIdWithRelationships(id);
        if (store == null) {
            logger.warn("Store not found with ID: {}", id);
            return null;
        }
        return storeMapper.getFrom(store);
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getAllStores(int page, int size) {
        logger.debug("Fetching stores with pagination - page: {}, size: {}", page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10; // Default page size
        }
        if (size > 100) {
            size = 100; // Maximum page size
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Store> storePage = storeRepository.findAllWithRelationships(pageable);
        
        List<StoreDto> stores = storeMapper.getFrom(storePage.getContent());
        
        return new PagedResponse<>(
            stores,
            storePage.getNumber(),
            storePage.getSize(),
            storePage.getTotalElements(),
            storePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresByCompany(Long companyId, int page, int size) {
        logger.debug("Fetching stores for company ID: {} with pagination - page: {}, size: {}", companyId, page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10; // Default page size
        }
        if (size > 100) {
            size = 100; // Maximum page size
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Store> storePage = storeRepository.findByCompanyIdWithRelationships(companyId, pageable);
        
        List<StoreDto> stores = storeMapper.getFrom(storePage.getContent());
        
        return new PagedResponse<>(
            stores,
            storePage.getNumber(),
            storePage.getSize(),
            storePage.getTotalElements(),
            storePage.getTotalPages()
        );
    }
}
