package com.pars.financial.service;

import java.util.List;
import java.util.Optional;

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
import com.pars.financial.dto.StoreCreateRequest;
import com.pars.financial.dto.StoreUpdateRequest;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.entity.Company;
import com.pars.financial.entity.Address;
import com.pars.financial.entity.PhoneNumber;
import com.pars.financial.enums.OwnershipType;
import com.pars.financial.enums.PhoneNumberType;
import com.pars.financial.mapper.StoreMapper;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.AddressRepository;
import com.pars.financial.repository.PhoneNumberRepository;

@Service
public class StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final SecurityContextService securityContextService;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final PhoneNumberRepository phoneNumberRepository;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, SecurityContextService securityContextService, 
                       CompanyRepository companyRepository, AddressRepository addressRepository, PhoneNumberRepository phoneNumberRepository) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.securityContextService = securityContextService;
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.phoneNumberRepository = phoneNumberRepository;
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

    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresByCompanyAndOwnershipType(Long companyId, OwnershipType ownershipType, int page, int size) {
        logger.info("Fetching stores for company ID: {} with ownership type: {} and pagination - page: {}, size: {}", companyId, ownershipType, page, size);
        
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
        // Use simple query method name - Spring Data will generate the query correctly
        logger.debug("Calling repository method: findByCompanyIdAndOwnershipTypeAndIsActiveTrue with companyId={}, ownershipType={}", companyId, ownershipType);
        Page<Store> storePage = storeRepository.findByCompanyIdAndOwnershipTypeAndIsActiveTrue(companyId, ownershipType, pageable);
        
        logger.info("Query returned {} stores out of {} total for companyId={}, ownershipType={}", 
                   storePage.getContent().size(), storePage.getTotalElements(), companyId, ownershipType);
        
        // Log each store's ownership type for debugging
        storePage.getContent().forEach(store -> 
            logger.debug("Store ID: {}, Name: {}, Company ID: {}, Ownership Type: {}", 
                        store.getId(), store.getStore_name(), 
                        store.getCompany() != null ? store.getCompany().getId() : "null",
                        store.getOwnershipType()));
        
        // Filter to ensure only matching stores are returned (safety check)
        List<Store> filteredStores = storePage.getContent().stream()
            .filter(store -> {
                boolean matches = store.getOwnershipType() == ownershipType && 
                                 store.getCompany() != null && 
                                 store.getCompany().getId().equals(companyId);
                if (!matches) {
                    logger.warn("Store {} doesn't match filter: ownershipType={} (expected {}), companyId={} (expected {})", 
                               store.getId(), store.getOwnershipType(), ownershipType,
                               store.getCompany() != null ? store.getCompany().getId() : "null", companyId);
                }
                return matches;
            })
            .toList();
        
        if (filteredStores.size() != storePage.getContent().size()) {
            logger.error("Query returned {} stores but only {} match the filter! Filtering out mismatched stores.", 
                        storePage.getContent().size(), filteredStores.size());
        }
        
        // Eagerly fetch relationships for the filtered stores
        List<Store> storesWithRelationships = filteredStores.stream()
            .map(store -> storeRepository.findByIdWithRelationships(store.getId()))
            .filter(store -> store != null)
            .toList();
        
        List<StoreDto> stores = storeMapper.getFrom(storesWithRelationships);
        
        return new PagedResponse<>(
            stores,
            storePage.getNumber(),
            storePage.getSize(),
            storePage.getTotalElements(),
            storePage.getTotalPages()
        );
    }

    /**
     * Get stores by ownership type based on user role and permissions
     * - SUPERADMIN/ADMIN/API_USER: All stores with the specified ownership type
     * - COMPANY_USER: Stores of their company with the specified ownership type
     * - STORE_USER: Only their assigned store if it matches the ownership type
     */
    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresForUserByOwnershipType(User user, OwnershipType ownershipType, int page, int size) {
        if (user == null || user.getRole() == null) {
            logger.warn("User or user role is null - returning empty result");
            return new PagedResponse<>(List.of(), 0, size > 0 ? size : 10, 0, 0);
        }
        
        logger.info("Fetching stores for user: {} with role: {} filtered by ownershipType: {} - page: {}, size: {}", 
                   user.getUsername(), user.getRole().getName(), ownershipType, page, size);
        
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
        String roleName = user.getRole().getName();
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // Return all stores with the specified ownership type
                logger.debug("User {} has admin access - returning all stores with ownershipType: {}", user.getUsername(), ownershipType);
                storePage = storeRepository.findByOwnershipTypeAndIsActiveTrue(ownershipType, pageable);
                break;
                
            case "COMPANY_USER":
                // Return stores of user's company with the specified ownership type
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    storePage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning stores for company: {} with ownershipType: {}", 
                               user.getUsername(), user.getCompany().getId(), ownershipType);
                    storePage = storeRepository.findByCompanyIdAndOwnershipTypeAndIsActiveTrue(user.getCompany().getId(), ownershipType, pageable);
                }
                break;
                
            case "STORE_USER":
                // Return only user's assigned store if it matches the ownership type
                if (user.getStore() == null) {
                    logger.warn("STORE_USER {} has no store assigned", user.getUsername());
                    storePage = Page.empty(pageable);
                } else {
                    // Check if the user's store matches the ownership type
                    if (user.getStore().getOwnershipType() == ownershipType && user.getStore().getIsActive()) {
                        logger.debug("User {} has store access - returning assigned store: {} with ownershipType: {}", 
                                   user.getUsername(), user.getStore().getId(), ownershipType);
                        List<Store> singleStore = List.of(user.getStore());
                        storePage = new org.springframework.data.domain.PageImpl<>(singleStore, pageable, 1);
                    } else {
                        logger.debug("User {} store doesn't match ownershipType filter: {}", user.getUsername(), ownershipType);
                        storePage = Page.empty(pageable);
                    }
                }
                break;
                
            default:
                logger.warn("Unknown role: {} for user: {}", roleName, user.getUsername());
                storePage = Page.empty(pageable);
                break;
        }
        
        // Eagerly fetch relationships for the returned stores
        List<Store> storesWithRelationships = storePage.getContent().stream()
            .map(store -> storeRepository.findByIdWithRelationships(store.getId()))
            .filter(store -> store != null)
            .filter(store -> store.getOwnershipType() == ownershipType) // Additional safety check
            .toList();
        
        List<StoreDto> stores = storeMapper.getFrom(storesWithRelationships);
        
        return new PagedResponse<>(
            stores,
            storePage.getNumber(),
            storePage.getSize(),
            storesWithRelationships.size(),
            (int) Math.ceil((double) storesWithRelationships.size() / size)
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
        if (user == null || user.getRole() == null) {
            logger.warn("User or user role is null - returning empty result");
            return new PagedResponse<>(List.of(), 0, size > 0 ? size : 10, 0, 0);
        }
        
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
        String roleName = user.getRole().getName();
        switch (roleName) {
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
                           roleName, user.getUsername());
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
     * Get stores with optional company and ownership type filters based on current user's permissions
     */
    @Transactional(readOnly = true)
    public PagedResponse<StoreDto> getStoresForCurrentUser(int page, int size, Long companyId, OwnershipType ownershipType) {
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
            // If ownershipType is also provided, filter by both
            if (ownershipType != null) {
                return getStoresByCompanyAndOwnershipType(companyId, ownershipType, page, size);
            } else {
                return getStoresByCompany(companyId, page, size);
            }
        } else {
            // No companyId provided - check if ownershipType filter is requested
            if (ownershipType != null) {
                return getStoresForUserByOwnershipType(currentUser, ownershipType, page, size);
            } else {
                return getStoresForUser(currentUser, page, size);
            }
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
        if (user == null || user.getRole() == null) {
            return false;
        }
        
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
        if (user == null || user.getRole() == null) {
            return false;
        }
        
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

    // ==================== CRUD OPERATIONS ====================

    @Transactional
    public StoreDto createStore(StoreCreateRequest request) {
        logger.info("Creating new store: {} for company: {}", request.getStoreName(), request.getCompanyId());
        
        // Validate company exists
        Company company = companyRepository.findById(request.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + request.getCompanyId()));
        
        Store store = new Store();
        store.setStore_name(request.getStoreName());
        store.setCompany(company);
        store.setOwnershipType(request.getOwnershipType());
        store.setLocationType(request.getLocationType());
        store.setIsActive(true);
        
        // Create and persist phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            // Check if phone number already exists
            Optional<PhoneNumber> existingPhoneNumber = phoneNumberRepository.findByNumber(request.getPhoneNumber());
            
            if (existingPhoneNumber.isPresent()) {
                // Reuse existing phone number
                logger.info("Reusing existing phone number: {} for store: {}", request.getPhoneNumber(), request.getStoreName());
                store.setPhone_number(existingPhoneNumber.get());
            } else {
                // Create new phone number
                logger.info("Creating new phone number: {} for store: {}", request.getPhoneNumber(), request.getStoreName());
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setNumber(request.getPhoneNumber());
                phoneNumber.setType(PhoneNumberType.Cell); // Default type
                PhoneNumber savedPhoneNumber = phoneNumberRepository.save(phoneNumber);
                store.setPhone_number(savedPhoneNumber);
            }
        }
        
        // Create and persist address if provided
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            Address address = new Address();
            address.setText(request.getAddress());
            address.setCity("Unknown"); // Default values
            address.setProvince("Unknown");
            address.setPostalCode("00000");
            Address savedAddress = addressRepository.save(address);
            store.setAddress(savedAddress);
        }
        
        Store savedStore = storeRepository.save(store);
        logger.info("Store created successfully with ID: {}", savedStore.getId());
        
        return storeMapper.getFrom(savedStore);
    }

    @Transactional
    public StoreDto updateStore(Long storeId, StoreUpdateRequest request) {
        logger.info("Updating store with ID: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        
        // Update basic fields if provided
        if (request.getStoreName() != null && !request.getStoreName().trim().isEmpty()) {
            store.setStore_name(request.getStoreName());
        }
        
        if (request.getOwnershipType() != null) {
            store.setOwnershipType(request.getOwnershipType());
        }
        
        if (request.getLocationType() != null) {
            store.setLocationType(request.getLocationType());
        }
        
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }
        
        // Update company if provided
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + request.getCompanyId()));
            store.setCompany(company);
        }
        
        // Update phone number if provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            // Check if phone number already exists
            Optional<PhoneNumber> existingPhoneNumber = phoneNumberRepository.findByNumber(request.getPhoneNumber());
            
            if (existingPhoneNumber.isPresent()) {
                // Reuse existing phone number
                logger.info("Reusing existing phone number: {} for store update: {}", request.getPhoneNumber(), storeId);
                store.setPhone_number(existingPhoneNumber.get());
            } else {
                // Create new phone number or update existing one
                PhoneNumber phoneNumber = store.getPhone_number();
                if (phoneNumber == null) {
                    logger.info("Creating new phone number: {} for store update: {}", request.getPhoneNumber(), storeId);
                    phoneNumber = new PhoneNumber();
                    phoneNumber.setType(PhoneNumberType.Cell); // Default type
                } else {
                    logger.info("Updating existing phone number: {} for store update: {}", request.getPhoneNumber(), storeId);
                }
                phoneNumber.setNumber(request.getPhoneNumber());
                PhoneNumber savedPhoneNumber = phoneNumberRepository.save(phoneNumber);
                store.setPhone_number(savedPhoneNumber);
            }
        }
        
        // Update address if provided
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            Address address = store.getAddress();
            if (address == null) {
                address = new Address();
                address.setCity("Unknown"); // Default values
                address.setProvince("Unknown");
                address.setPostalCode("00000");
            }
            address.setText(request.getAddress());
            Address savedAddress = addressRepository.save(address);
            store.setAddress(savedAddress);
        }
        
        Store savedStore = storeRepository.save(store);
        logger.info("Store updated successfully with ID: {}", savedStore.getId());
        
        return storeMapper.getFrom(savedStore);
    }

    @Transactional
    public void deleteStore(Long storeId) {
        logger.info("Logically deleting store with ID: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        
        store.setIsActive(false);
        storeRepository.save(store);
        
        logger.info("Store logically deleted successfully with ID: {}", storeId);
    }

}
