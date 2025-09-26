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
import com.pars.financial.entity.User;
import com.pars.financial.enums.UserRole;
import com.pars.financial.mapper.StoreMapper;
import com.pars.financial.repository.StoreRepository;

@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final SecurityContextService securityContextService;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, SecurityContextService securityContextService) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.securityContextService = securityContextService;
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

    /**
     * Get stores based on user role and permissions
     * - SUPERADMIN/ADMIN: All stores
     * - COMPANY_USER: Stores of their company
     * - STORE_USER: Only their assigned store
     * - API_USER: All stores (if system-wide access needed)
     */
    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresForUser(User user, int page, int size) {
        logger.debug("Fetching stores for user: {} with role: {} - page: {}, size: {}", 
                    user.getUsername(), user.getRole().getName(), page, size);
        
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
        Page<Store> storePage;
        
        // Role-based data filtering
        switch (user.getRole().getName()) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // Return all stores
                logger.debug("User {} has admin access - returning all stores", user.getUsername());
                storePage = storeRepository.findAllWithRelationships(pageable);
                break;
                
            case "COMPANY_USER":
                // Return stores of user's company
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    storePage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning stores for company: {}", 
                               user.getUsername(), user.getCompany().getId());
                    storePage = storeRepository.findByCompanyIdWithRelationships(user.getCompany().getId(), pageable);
                }
                break;
                
            case "STORE_USER":
                // Return only user's assigned store
                if (user.getStore() == null) {
                    logger.warn("STORE_USER {} has no store assigned", user.getUsername());
                    storePage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has store access - returning assigned store: {}", 
                               user.getUsername(), user.getStore().getId());
                    // Create a single-item page for the user's store
                    List<Store> singleStore = List.of(user.getStore());
                    storePage = new org.springframework.data.domain.PageImpl<>(singleStore, pageable, 1);
                }
                break;
                
            default:
                logger.warn("Unknown role {} for user {} - returning empty result", 
                           user.getRole().getName(), user.getUsername());
                storePage = Page.empty(pageable);
                break;
        }
        
        List<StoreDto> stores = storeMapper.getFrom(storePage.getContent());
        
        return new PagedResponse<>(
            stores,
            storePage.getNumber(),
            storePage.getSize(),
            storePage.getTotalElements(),
            storePage.getTotalPages()
        );
    }

    /**
     * Get stores based on current user's role and permissions
     * - SUPERADMIN/ADMIN: All stores
     * - COMPANY_USER: Stores of their company
     * - STORE_USER: Only their assigned store
     * - API_USER: All stores (if system-wide access needed)
     */
    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresForCurrentUser(int page, int size) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }
        
        return getStoresForUser(currentUser, page, size);
    }

    /**
     * Get stores with optional company filter based on current user's permissions
     */
    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresForCurrentUser(int page, int size, Long companyId) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }

        // If companyId is provided, validate user has permission to filter by it
        if (companyId != null) {
            if (!canUserFilterByCompany(currentUser, companyId)) {
                logger.warn("User {} attempted to filter by company {} without permission", 
                           currentUser.getUsername(), companyId);
                return new PagedResponse<>(List.of(), 0, size, 0, 0);
            }
            return getStoresByCompany(companyId, page, size);
        } else {
            return getStoresForUser(currentUser, page, size);
        }
    }

    /**
     * Get a specific store with access control
     */
    @Transactional(readOnly = true)
    public StoreDto getStoreForCurrentUser(Long storeId) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return null;
        }

        StoreDto store = getStore(storeId);
        if (store == null) {
            return null;
        }

        // Check if user has access to this specific store
        if (!hasAccessToStore(currentUser, store)) {
            logger.warn("User {} attempted to access store {} without permission", 
                       currentUser.getUsername(), storeId);
            return null;
        }

        return store;
    }

    /**
     * Check if user can filter by a specific company
     */
    private boolean canUserFilterByCompany(User user, Long companyId) {
        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;
                
            case "COMPANY_USER":
                return user.getCompany() != null && user.getCompany().getId().equals(companyId);
                
            case "STORE_USER":
                return user.getStore() != null && 
                       user.getStore().getCompany() != null && 
                       user.getStore().getCompany().getId().equals(companyId);
                       
            default:
                return false;
        }
    }

    /**
     * Check if user has access to a specific store
     */
    private boolean hasAccessToStore(User user, StoreDto store) {
        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;
                
            case "COMPANY_USER":
                return user.getCompany() != null && 
                       store.company != null && 
                       user.getCompany().getId().equals(store.company.getId());
                       
            case "STORE_USER":
                return user.getStore() != null && 
                       user.getStore().getId().equals(store.id);
                       
            default:
                return false;
        }
    }
}
