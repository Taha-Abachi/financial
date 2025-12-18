package com.pars.financial.service;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.dto.DiscountCodeReportDto;
import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.dto.DiscountCodeValidationResponse;
import com.pars.financial.dto.PagedResponse;

import com.pars.financial.entity.DiscountCode;
import com.pars.financial.enums.DiscountType;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.DiscountCodeMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.repository.DiscountCodeTransactionRepository;
import com.pars.financial.repository.ItemCategoryRepository;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.entity.User;
import com.pars.financial.entity.Customer;
import com.pars.financial.enums.DiscountCodeType;
import com.pars.financial.utils.RandomStringGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DiscountCodeService {

    @Value("${spring.application.discountcode.len}")
    private int discountCodeLength;

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeService.class);

    private final DiscountCodeRepository codeRepository;
    private final DiscountCodeMapper mapper;
    private final CompanyRepository companyRepository;
    private final StoreRepository storeRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final CustomerRepository customerRepository;
    private final DiscountCodeTransactionRepository discountCodeTransactionRepository;
    private final SecurityContextService securityContextService;
    private final CustomerService customerService;

    public DiscountCodeService(DiscountCodeRepository codeRepository, DiscountCodeMapper mapper, CompanyRepository companyRepository, StoreRepository storeRepository, ItemCategoryRepository itemCategoryRepository, CustomerRepository customerRepository, DiscountCodeTransactionRepository discountCodeTransactionRepository, SecurityContextService securityContextService, CustomerService customerService) {
        this.codeRepository = codeRepository;
        this.mapper = mapper;
        this.companyRepository = companyRepository;
        this.storeRepository = storeRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.customerRepository = customerRepository;
        this.discountCodeTransactionRepository = discountCodeTransactionRepository;
        this.securityContextService = securityContextService;
        this.customerService = customerService;
    }

    private DiscountCode issueDiscountCode(int percentage, long validityPeriod, long maxDiscountAmount, long minimumBillAmount, int usageLimit, long constantDiscountAmount, DiscountType discountType, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String customCode, Long customSerialNo, DiscountCodeType type, String phoneNumber) {
        return issueDiscountCode(percentage, validityPeriod, maxDiscountAmount, minimumBillAmount, usageLimit, constantDiscountAmount, discountType, companyId, storeLimited, allowedStoreIds, itemCategoryLimited, allowedItemCategoryIds, customCode, customSerialNo, type, phoneNumber, null);
    }

    private DiscountCode issueDiscountCode(int percentage, long validityPeriod, long maxDiscountAmount, long minimumBillAmount, int usageLimit, long constantDiscountAmount, DiscountType discountType, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String customCode, Long customSerialNo, DiscountCodeType type, String phoneNumber, com.pars.financial.entity.Batch batch) {
        logger.debug("Issuing new discount code with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}, itemCategoryLimited: {}, allowedItemCategoryIds: {}, customCode: {}, customSerialNo: {}", 
            percentage, validityPeriod, maxDiscountAmount, minimumBillAmount, usageLimit, constantDiscountAmount, discountType, companyId, storeLimited, allowedStoreIds, itemCategoryLimited, allowedItemCategoryIds, customCode, customSerialNo);
        var code = new DiscountCode();
        code.setIssueDate(LocalDateTime.now());
        code.setExpiryDate(LocalDate.now().plusDays(validityPeriod));
        
        // Use custom code if provided, otherwise generate random
        if (customCode != null && !customCode.trim().isEmpty()) {
            code.setCode(customCode);
        } else {
            code.setCode("DC" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(discountCodeLength - 2));
        }
        
        // Use custom serial number if provided, otherwise generate random
        if (customSerialNo != null) {
            code.setSerialNo(customSerialNo);
        } else {
            code.setSerialNo(ThreadLocalRandom.current().nextLong(10000000, 100000000));
        }
        
        code.setPercentage(percentage);
        code.setMaxDiscountAmount(maxDiscountAmount);
        code.setMinimumBillAmount(minimumBillAmount);
        code.setUsageLimit(usageLimit);
        code.setCurrentUsageCount(0);
        code.setConstantDiscountAmount(constantDiscountAmount);
        code.setDiscountType(discountType);
        code.setStoreLimited(storeLimited);
        if (storeLimited && allowedStoreIds != null && !allowedStoreIds.isEmpty()) {
            java.util.Set<com.pars.financial.entity.Store> stores = new java.util.HashSet<>();
            for (Long storeId : allowedStoreIds) {
                var storeOpt = storeRepository.findById(storeId);
                if (storeOpt.isPresent()) {
                    stores.add(storeOpt.get());
                } else {
                    logger.warn("Store not found with id: {} while assigning to discount code", storeId);
                }
            }
            code.setAllowedStores(stores);
        }
        
        code.setItemCategoryLimited(itemCategoryLimited);
        if (itemCategoryLimited && allowedItemCategoryIds != null && !allowedItemCategoryIds.isEmpty()) {
            java.util.Set<com.pars.financial.entity.ItemCategory> itemCategories = new java.util.HashSet<>();
            for (Long itemCategoryId : allowedItemCategoryIds) {
                var itemCategoryOpt = itemCategoryRepository.findById(itemCategoryId);
                if (itemCategoryOpt.isPresent()) {
                    itemCategories.add(itemCategoryOpt.get());
                } else {
                    logger.warn("Item category not found with id: {} while assigning to discount code", itemCategoryId);
                }
            }
            code.setAllowedItemCategories(itemCategories);
        }
        // Set company if companyId is provided
        if (companyId != null) {
            var company = companyRepository.findById(companyId);
            if (company.isEmpty()) {
                logger.warn("Company not found with ID: {}", companyId);
                throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
            }
            code.setCompany(company.get());
            logger.debug("Assigned discount code to company: {}", company.get().getName());
        }
        
        // Set type (defaults to GENERAL if null via setType method)
        code.setType(type);
        
        // Handle customer assignment for PERSONAL type
        if (code.getType() == DiscountCodeType.PERSONAL) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                logger.error("Phone number is required for PERSONAL discount code type");
                throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Phone number is required when type is PERSONAL");
            }
            
            // Check if customer exists, if not create one
            Customer customer = customerRepository.findByPrimaryPhoneNumber(phoneNumber);
            if (customer == null) {
                logger.info("Customer not found for phone number: {}, creating new customer", phoneNumber);
                customer = customerService.createCustomer(phoneNumber);
            } else {
                logger.debug("Found existing customer with ID: {} for phone number: {}", customer.getId(), phoneNumber);
            }
            code.setCustomer(customer);
        } else {
            // For GENERAL type, ignore phone number if provided
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                logger.warn("Phone number provided for GENERAL discount code type, ignoring it");
            }
            code.setCustomer(null);
        }
        
        // Set batch reference if provided
        if (batch != null) {
            code.setBatch(batch);
        }
        
        logger.debug("Created discount code: {} with type: {}", code.getCode(), code.getType());
        return code;
    }

    public DiscountCodeDto generate(DiscountCodeIssueRequest dto) {
        logger.info("Generating new discount code with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}, itemCategoryLimited: {}, allowedItemCategoryIds: {}, customCode: {}, customSerialNo: {}, type: {}, phoneNumber: {}", 
            dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds, dto.itemCategoryLimited, dto.allowedItemCategoryIds, dto.code, dto.serialNo, dto.type, dto.phoneNumber);
        
        // Validate type and phoneNumber
        DiscountCodeType type = dto.type != null ? dto.type : DiscountCodeType.GENERAL;
        if (type == DiscountCodeType.PERSONAL) {
            if (dto.phoneNumber == null || dto.phoneNumber.trim().isEmpty()) {
                logger.error("Phone number is required for PERSONAL discount code type");
                throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Phone number is required when type is PERSONAL");
            }
        }
        // For GENERAL type, phone number is ignored if provided
        
        // Validate: if custom code is provided, count must be exactly 1
        boolean hasCustomCode = dto.code != null && !dto.code.trim().isEmpty();
        if (hasCustomCode && dto.count != 1) {
            logger.error("When providing a custom discount code, count must be exactly 1. Provided count: {}", dto.count);
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "When providing a custom discount code, count must be exactly 1");
        }
        
        // Check if both code and serialNo are provided simultaneously and count is 1
        boolean hasCustomSerialNo = dto.serialNo != null;
        boolean isSingleCode = dto.count == 1;
        
        if (hasCustomCode && hasCustomSerialNo && isSingleCode) {
            // Check if the provided code and serialNo are not already in the database
            if (codeRepository.existsByCode(dto.code)) {
                logger.warn("Discount code already exists: {}", dto.code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_ALREADY_EXISTS);
            }
            if (codeRepository.existsBySerialNo(dto.serialNo)) {
                logger.warn("Serial number already exists: {}", dto.serialNo);
                throw new ValidationException(ErrorCodes.SERIAL_NUMBER_ALREADY_EXISTS);
            }
            logger.info("Using provided custom code: {} and serial number: {}", dto.code, dto.serialNo);
        } else if (hasCustomCode || hasCustomSerialNo) {
            // If only one of them is provided, or count is not 1, ignore custom values and generate random
            logger.warn("Both code and serialNo must be provided simultaneously and count must be 1 to use custom values. Generating random values instead.");
            dto.code = null;
            dto.serialNo = null;
        }
        
        var discountCode = issueDiscountCode(dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds, dto.itemCategoryLimited, dto.allowedItemCategoryIds, dto.code, dto.serialNo, type, dto.phoneNumber);
        var savedCode = codeRepository.save(discountCode);
        logger.info("Generated discount code: {} with type: {}", savedCode.getCode(), savedCode.getType());
        return mapper.getFrom(savedCode);
    }

    public List<DiscountCodeDto> generateList(DiscountCodeIssueRequest request) {
        request.storeLimited = !request.allowedStoreIds.isEmpty();
        request.itemCategoryLimited = !request.allowedItemCategoryIds.isEmpty();
        logger.info("Generating {} discount codes with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}, itemCategoryLimited: {}, allowedItemCategoryIds: {}, customCode: {}, customSerialNo: {}, type: {}, phoneNumber: {}", 
            request.count, request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds, request.itemCategoryLimited, request.allowedItemCategoryIds, request.code, request.serialNo, request.type, request.phoneNumber);
        
        // Validate type - PERSONAL type is not allowed in list generation
        DiscountCodeType type = request.type != null ? request.type : DiscountCodeType.GENERAL;
        if (type == DiscountCodeType.PERSONAL) {
            logger.error("PERSONAL discount codes can only be created using single code creation endpoint, not in lists");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "PERSONAL discount codes can only be created one at a time using the single code creation endpoint");
        }
        // For GENERAL type, phone number is ignored if provided
        
        // Validate: if custom code is provided, count must be exactly 1
        boolean hasCustomCode = request.code != null && !request.code.trim().isEmpty();
        if (hasCustomCode && request.count != 1) {
            logger.error("When providing a custom discount code, count must be exactly 1. Provided count: {}", request.count);
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "When providing a custom discount code, count must be exactly 1");
        }
        
        // Check if both code and serialNo are provided simultaneously and count is 1
        boolean hasCustomSerialNo = request.serialNo != null;
        boolean isSingleCode = request.count == 1;
        
        if (hasCustomCode && hasCustomSerialNo && isSingleCode) {
            // Check if the provided code and serialNo are not already in the database
            if (codeRepository.existsByCode(request.code)) {
                logger.warn("Discount code already exists: {}", request.code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_ALREADY_EXISTS);
            }
            if (codeRepository.existsBySerialNo(request.serialNo)) {
                logger.warn("Serial number already exists: {}", request.serialNo);
                throw new ValidationException(ErrorCodes.SERIAL_NUMBER_ALREADY_EXISTS);
            }
            logger.info("Using provided custom code: {} and serial number: {}", request.code, request.serialNo);
        } else if (hasCustomCode || hasCustomSerialNo) {
            // If only one of them is provided, or count is not 1, ignore custom values and generate random
            logger.warn("Both code and serialNo must be provided simultaneously and count must be 1 to use custom values. Generating random values instead.");
            request.code = null;
            request.serialNo = null;
        }
        
        var ls = new ArrayList<DiscountCode>();
        for (var i = 0; i < request.count; i++) {
            var discountCode = issueDiscountCode(request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds, request.itemCategoryLimited, request.allowedItemCategoryIds, request.code, request.serialNo, type, request.phoneNumber);
            ls.add(discountCode);
        }
        var savedCodes = codeRepository.saveAll(ls);
        logger.info("Generated {} discount codes successfully with type: {}", request.count, type);
        return mapper.getFrom(savedCodes);
    }

    public List<DiscountCodeDto> generateList(DiscountCodeIssueRequest request, com.pars.financial.entity.Batch batch) {
        request.storeLimited = !request.allowedStoreIds.isEmpty();
        request.itemCategoryLimited = !request.allowedItemCategoryIds.isEmpty();
        logger.info("Generating {} discount codes with request: {} for batch: {}", request.count, request, batch.getBatchNumber());
        
        // Validate type - PERSONAL type is not allowed in list generation (including batch creation)
        DiscountCodeType type = request.type != null ? request.type : DiscountCodeType.GENERAL;
        if (type == DiscountCodeType.PERSONAL) {
            logger.error("PERSONAL discount codes can only be created using single code creation endpoint, not in lists or batches");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "PERSONAL discount codes can only be created one at a time using the single code creation endpoint");
        }
        // For GENERAL type, phone number is ignored if provided
        
        // Validate: if custom code is provided, count must be exactly 1
        boolean hasCustomCode = request.code != null && !request.code.trim().isEmpty();
        if (hasCustomCode && request.count != 1) {
            logger.error("When providing a custom discount code, count must be exactly 1. Provided count: {}", request.count);
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "When providing a custom discount code, count must be exactly 1");
        }
        
        // Check if both code and serialNo are provided simultaneously and count is 1
        boolean hasCustomSerialNo = request.serialNo != null;
        boolean isSingleCode = request.count == 1;
        
        if (hasCustomCode && hasCustomSerialNo && isSingleCode) {
            // Check if the provided code and serialNo are not already in the database
            if (codeRepository.existsByCode(request.code)) {
                logger.warn("Discount code already exists: {}", request.code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_ALREADY_EXISTS);
            }
            if (codeRepository.existsBySerialNo(request.serialNo)) {
                logger.warn("Serial number already exists: {}", request.serialNo);
                throw new ValidationException(ErrorCodes.SERIAL_NUMBER_ALREADY_EXISTS);
            }
            logger.info("Using provided custom code: {} and serial number: {}", request.code, request.serialNo);
        } else if (hasCustomCode || hasCustomSerialNo) {
            // If only one of them is provided, or count is not 1, ignore custom values and generate random
            logger.warn("Both code and serialNo must be provided simultaneously and count must be 1 to use custom values. Generating random values instead.");
            request.code = null;
            request.serialNo = null;
        }
        
        var ls = new ArrayList<DiscountCode>();
        for (var i = 0; i < request.count; i++) {
            var discountCode = issueDiscountCode(request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds, request.itemCategoryLimited, request.allowedItemCategoryIds, request.code, request.serialNo, type, request.phoneNumber, batch);
            ls.add(discountCode);
        }
        var savedCodes = codeRepository.saveAll(ls);
        logger.info("Generated {} discount codes successfully for batch: {} with type: {}", request.count, batch.getBatchNumber(), type);
        return mapper.getFrom(savedCodes);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DiscountCodeDto> getAllDiscountCodes(int page, int size) {
        logger.debug("Fetching discount codes with pagination - page: {}, size: {}", page, size);
        
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
        Page<DiscountCode> discountCodePage = codeRepository.findAll(pageable);
        
        List<DiscountCodeDto> discountCodes = mapper.getFrom(discountCodePage.getContent());
        
        return new PagedResponse<>(
            discountCodes,
            discountCodePage.getNumber(),
            discountCodePage.getSize(),
            discountCodePage.getTotalElements(),
            discountCodePage.getTotalPages()
        );
    }

    public DiscountCodeDto getDiscountCode(String code) {
        logger.debug("Fetching discount code: {}", code);
        var discountCode = codeRepository.findByCode(code);
        if (discountCode == null) {
            logger.warn("Discount code not found: {}", code);
        }
        return mapper.getFrom(discountCode);
    }

    public void limitToStores(String code, List<Long> storeIds) {
        logger.info("Limiting discount code {} to stores: {}", code, storeIds);
        var discountCode = codeRepository.findByCode(code);
        if (discountCode == null) {
            logger.warn("Discount code not found with code: {}", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND);
        }

        if (storeIds == null || storeIds.isEmpty()) {
            logger.debug("Removing store limitations for discount code: {}", code);
            discountCode.setStoreLimited(false);
            discountCode.setAllowedStores(new java.util.HashSet<>());
        } else {
            java.util.Set<com.pars.financial.entity.Store> stores = new java.util.HashSet<>();
            for (Long storeId : storeIds) {
                var store = storeRepository.findById(storeId)
                    .orElseThrow(() -> {
                        logger.warn("Store not found with id: {}", storeId);
                        return new ValidationException(ErrorCodes.STORE_NOT_FOUND, "Store not found with id: " + storeId);
                    });
                stores.add(store);
            }
            discountCode.setStoreLimited(true);
            discountCode.setAllowedStores(stores);
            logger.debug("Limited discount code {} to {} stores", code, stores.size());
        }
        codeRepository.save(discountCode);
        logger.info("Successfully updated store limitations for discount code: {}", code);
    }

    public void removeStoreLimitation(String code) {
        logger.info("Removing store limitations for discount code: {}", code);
        var discountCode = codeRepository.findByCode(code);
        if (discountCode == null) {
            logger.warn("Discount code not found with code: {}", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND);
        }
        discountCode.setStoreLimited(false);
        discountCode.setAllowedStores(new java.util.HashSet<>());
        codeRepository.save(discountCode);
        logger.info("Successfully removed store limitations for discount code: {}", code);
    }

    public void limitToItemCategories(String code, List<Long> itemCategoryIds) {
        logger.info("Limiting discount code {} to item categories: {}", code, itemCategoryIds);
        var discountCode = codeRepository.findByCode(code);
        if (discountCode == null) {
            logger.warn("Discount code not found with code: {}", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND);
        }

        if (itemCategoryIds == null || itemCategoryIds.isEmpty()) {
            logger.debug("Removing item category limitations for discount code: {}", code);
            discountCode.setItemCategoryLimited(false);
            discountCode.setAllowedItemCategories(new java.util.HashSet<>());
        } else {
            java.util.Set<com.pars.financial.entity.ItemCategory> itemCategories = new java.util.HashSet<>();
            for (Long itemCategoryId : itemCategoryIds) {
                var itemCategory = itemCategoryRepository.findById(itemCategoryId)
                    .orElseThrow(() -> {
                        logger.warn("Item category not found with id: {}", itemCategoryId);
                        return new ValidationException(ErrorCodes.ITEM_CATEGORY_NOT_FOUND, "Item category not found with id: " + itemCategoryId);
                    });
                itemCategories.add(itemCategory);
            }
            discountCode.setItemCategoryLimited(true);
            discountCode.setAllowedItemCategories(itemCategories);
            logger.debug("Limited discount code {} to {} item categories", code, itemCategories.size());
        }
        codeRepository.save(discountCode);
        logger.info("Successfully updated item category limitations for discount code: {}", code);
    }

    public void removeItemCategoryLimitation(String code) {
        logger.info("Removing item category limitations for discount code: {}", code);
        var discountCode = codeRepository.findByCode(code);
        if (discountCode == null) {
            logger.warn("Discount code not found with code: {}", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND);
        }
        discountCode.setItemCategoryLimited(false);
        discountCode.setAllowedItemCategories(new java.util.HashSet<>());
        codeRepository.save(discountCode);
        logger.info("Successfully removed item category limitations for discount code: {}", code);
    }

    /**
     * Shared validation method for discount code redemption rules
     * This method can be used by both check and redeem operations
     */
    public DiscountCodeValidationResponse validateDiscountCodeRules(DiscountCodeTransactionDto request) {
        var response = new DiscountCodeValidationResponse();
        
        var code = codeRepository.findByCode(request.code);
        if(code == null) {
            response.isValid = false;
            response.message = "Discount Code not found.";
            response.errorCode = "DISCOUNT_CODE_NOT_FOUND";
            return response;
        }
        
        // Check if discount code is blocked
        if(code.isBlocked()) {
            response.isValid = false;
            response.message = "Discount code is blocked and cannot be used.";
            response.errorCode = "DISCOUNT_CODE_BLOCKED";
            return response;
        }
        
        if(LocalDate.now().isAfter(code.getExpiryDate())){
            response.isValid = false;
            response.message = "Discount code is expired.";
            response.errorCode = "DISCOUNT_CODE_EXPIRED";
            return response;
        }
        
        if(!code.isActive()) {
            response.isValid = false;
            response.message = "Discount code is inactive.";
            response.errorCode = "DISCOUNT_CODE_INACTIVE";
            return response;
        }
        
        if(code.isUsed()) {
            response.isValid = false;
            response.message = "Discount already used.";
            response.errorCode = "DISCOUNT_CODE_ALREADY_USED";
            return response;
        }
        
        if (code.getCurrentUsageCount() >= code.getUsageLimit()) {
            response.isValid = false;
            response.message = "Discount code usage limit reached.";
            response.errorCode = "DISCOUNT_CODE_USAGE_LIMIT_REACHED";
            return response;
        }
        
        if (code.getMinimumBillAmount() > 0 && request.originalAmount < code.getMinimumBillAmount()) {
            response.isValid = false;
            response.message = "Original amount is less than minimum bill amount required.";
            response.errorCode = "MINIMUM_BILL_AMOUNT_NOT_MET";
            return response;
        }
        
        var store = storeRepository.findById(request.storeId);
        if (store.isEmpty()) {
            response.isValid = false;
            response.message = "Store Not Found";
            response.errorCode = "STORE_NOT_FOUND";
            return response;
        }
        
        // Store limitation check
        if (code.isStoreLimited()) {
            boolean storeAllowed = code.getAllowedStores().stream()
                .anyMatch(s -> s.getId().equals(request.storeId));
            if (!storeAllowed) {
                response.isValid = false;
                response.message = "Discount code cannot be used in this store";
                response.errorCode = "STORE_NOT_ALLOWED";
                return response;
            }
        }
        
        // Item category limitation check (if implemented)
        if (code.isItemCategoryLimited()) {
            // This would need to be implemented based on your business logic
            // For now, we'll just set the flag
            response.itemCategoryLimited = true;
        }
        
        // Calculate discount amount
        long discount;
        switch (code.getDiscountType()) {
            case FREEDELIVERY -> {
                discount = 0;
            }
            case CONSTANT -> {
                discount = code.getConstantDiscountAmount();
            }
            default -> {
                // PERCENTAGE
                discount = (long) (code.getPercentage() * request.originalAmount / 100);
                if(code.getMaxDiscountAmount() > 0) {
                    discount = Math.min(discount, code.getMaxDiscountAmount());
                }
            }
        }
        
        // Set response data
        response.isValid = true;
        response.message = "Discount code is valid";
        response.calculatedDiscountAmount = discount;
        response.discountType = code.getDiscountType();
        response.percentage = code.getPercentage();
        response.maxDiscountAmount = code.getMaxDiscountAmount();
        response.minimumBillAmount = code.getMinimumBillAmount();
        response.usageLimit = code.getUsageLimit();
        response.currentUsageCount = code.getCurrentUsageCount();
        response.constantDiscountAmount = code.getConstantDiscountAmount();
        response.storeLimited = code.isStoreLimited();
        response.itemCategoryLimited = code.isItemCategoryLimited();
        
        return response;
    }

    /**
     * Generate comprehensive discount code report for all discount codes
     * @return DiscountCodeReportDto containing all statistics
     */
    public DiscountCodeReportDto generateDiscountCodeReport() {
        logger.info("Generating comprehensive discount code report");
        
        try {
            // Get basic statistics
            Long totalCount = codeRepository.countAllDiscountCodes();
            Long totalUsedCount = codeRepository.countUsedDiscountCodes();
            Long totalUnusedCount = codeRepository.countUnusedDiscountCodes();
            Long totalActiveCount = codeRepository.countActiveDiscountCodes();
            Long totalInactiveCount = codeRepository.countInactiveDiscountCodes();
            Long totalExpiredCount = codeRepository.countExpiredDiscountCodes(LocalDate.now());
            Double averagePercentage = codeRepository.getAveragePercentage();
            Double averageMaxDiscountAmount = codeRepository.getAverageMaxDiscountAmount();
            Double averageMinimumBillAmount = codeRepository.getAverageMinimumBillAmount();
            Double averageUsageCount = codeRepository.getAverageUsageCount();
            Double averageUsageLimit = codeRepository.getAverageUsageLimit();
            
            // Get redeem transaction statistics
            Long totalRedeemTransactions = discountCodeTransactionRepository.countRedeemTransactions();
            Long totalDiscountAmount = discountCodeTransactionRepository.sumDiscountAmount();
            Long totalOriginalAmount = discountCodeTransactionRepository.sumOriginalAmount();
            
            DiscountCodeReportDto report = new DiscountCodeReportDto(
                totalCount, totalUsedCount, totalUnusedCount,
                totalActiveCount, totalInactiveCount, totalExpiredCount,
                totalRedeemTransactions, totalDiscountAmount, totalOriginalAmount,
                averagePercentage, averageMaxDiscountAmount, averageMinimumBillAmount,
                averageUsageCount, averageUsageLimit
            );
            
            logger.info("Successfully generated discount code report: totalCount={}, totalUsedCount={}, totalRedeemTransactions={}", 
                       totalCount, totalUsedCount, totalRedeemTransactions);
            
            return report;
            
        } catch (Exception e) {
            logger.error("Error generating discount code report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate discount code report", e);
        }
    }

    /**
     * Generate discount code report for a specific company
     * @param companyId the company ID
     * @return DiscountCodeReportDto containing company-specific statistics
     */
    public DiscountCodeReportDto generateDiscountCodeReportByCompany(Long companyId) {
        logger.info("Generating discount code report for company: {}", companyId);
        
        try {
            // Validate company exists
            if (!companyRepository.existsById(companyId)) {
                throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
            }
            
            // Get company-specific statistics
            Long totalCount = codeRepository.countDiscountCodesByCompany(companyId);
            Long totalUsedCount = codeRepository.countUsedDiscountCodesByCompany(companyId);
            Long totalUnusedCount = codeRepository.countUnusedDiscountCodesByCompany(companyId);
            Long totalActiveCount = codeRepository.countActiveDiscountCodesByCompany(companyId);
            Long totalInactiveCount = codeRepository.countInactiveDiscountCodesByCompany(companyId);
            Long totalExpiredCount = codeRepository.countExpiredDiscountCodesByCompany(companyId, LocalDate.now());
            Double averagePercentage = codeRepository.getAveragePercentageByCompany(companyId);
            Double averageMaxDiscountAmount = codeRepository.getAverageMaxDiscountAmountByCompany(companyId);
            Double averageMinimumBillAmount = codeRepository.getAverageMinimumBillAmountByCompany(companyId);
            Double averageUsageCount = codeRepository.getAverageUsageCountByCompany(companyId);
            Double averageUsageLimit = codeRepository.getAverageUsageLimitByCompany(companyId);
            
            // Get company-specific redeem transaction statistics
            Long totalRedeemTransactions = discountCodeTransactionRepository.countRedeemTransactionsByCompany(companyId);
            Long totalDiscountAmount = discountCodeTransactionRepository.sumDiscountAmountByCompany(companyId);
            Long totalOriginalAmount = discountCodeTransactionRepository.sumOriginalAmountByCompany(companyId);
            
            DiscountCodeReportDto report = new DiscountCodeReportDto(
                totalCount, totalUsedCount, totalUnusedCount,
                totalActiveCount, totalInactiveCount, totalExpiredCount,
                totalRedeemTransactions, totalDiscountAmount, totalOriginalAmount,
                averagePercentage, averageMaxDiscountAmount, averageMinimumBillAmount,
                averageUsageCount, averageUsageLimit
            );
            
            logger.info("Successfully generated discount code report for company {}: totalCount={}, totalUsedCount={}, totalRedeemTransactions={}", 
                       companyId, totalCount, totalUsedCount, totalRedeemTransactions);
            
            return report;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating discount code report for company {}: {}", companyId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate discount code report for company", e);
        }
    }

    /**
     * Get discount codes for current user with RBAC filtering
     * @param user the current user
     * @param page page number
     * @param size page size
     * @param companyId optional company filter (SUPERADMIN only)
     * @param storeId optional store filter (SUPERADMIN only)
     * @return PagedResponse of discount codes
     */
    @Transactional(readOnly = true)
    public PagedResponse<DiscountCodeDto> getDiscountCodesForCurrentUserWithFiltering(User user, int page, int size, Long companyId, Long storeId) {
        logger.debug("Fetching discount codes for user: {} with role: {} - page: {}, size: {}, companyId: {}, storeId: {}",
                    user.getUsername(), user.getRole().getName(), page, size, companyId, storeId);

        if (page < 0) { page = 0; }
        if (size <= 0) { size = 10; }
        if (size > 100) { size = 100; }

        Pageable pageable = PageRequest.of(page, size);
        Page<DiscountCode> discountCodePage;

        switch (user.getRole().getName()) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                if (companyId != null) {
                    var company = companyRepository.findById(companyId);
                    if (company.isEmpty()) {
                        logger.warn("Company not found with ID: {}", companyId);
                        throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
                    }
                    if (storeId != null) {
                        var store = storeRepository.findById(storeId);
                        if (store.isEmpty()) {
                            logger.warn("Store not found with ID: {}", storeId);
                            throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                        }
                        if (!store.get().getCompany().getId().equals(companyId)) {
                            logger.warn("Store {} does not belong to company {}", storeId, companyId);
                            throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                        }
                        discountCodePage = codeRepository.findByCompany(company.get(), pageable);
                    } else {
                        discountCodePage = codeRepository.findByCompany(company.get(), pageable);
                    }
                } else if (storeId != null) {
                    var store = storeRepository.findById(storeId);
                    if (store.isEmpty()) {
                        logger.warn("Store not found with ID: {}", storeId);
                        throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                    }
                    discountCodePage = codeRepository.findByCompany(store.get().getCompany(), pageable);
                } else {
                    discountCodePage = codeRepository.findAll(pageable);
                }
                break;
            case "COMPANY_USER":
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    discountCodePage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning discount codes for company: {}",
                               user.getUsername(), user.getCompany().getId());
                    discountCodePage = codeRepository.findByCompany(user.getCompany(), pageable);
                }
                break;
            case "STORE_USER":
                if (user.getStore() == null) {
                    logger.warn("STORE_USER {} has no store assigned", user.getUsername());
                    discountCodePage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has store access - returning discount codes for store's company: {}",
                               user.getUsername(), user.getStore().getCompany().getId());
                    discountCodePage = codeRepository.findByCompany(user.getStore().getCompany(), pageable);
                }
                break;
            default:
                logger.warn("Unknown role {} for user {} - returning empty result",
                           user.getRole().getName(), user.getUsername());
                discountCodePage = Page.empty(pageable);
                break;
        }
        List<DiscountCodeDto> discountCodes = mapper.getFrom(discountCodePage.getContent());
        return new PagedResponse<>(
            discountCodes,
            discountCodePage.getNumber(),
            discountCodePage.getSize(),
            discountCodePage.getTotalElements(),
            discountCodePage.getTotalPages()
        );
    }

    /**
     * Check if user has access to a specific discount code
     * @param user the current user
     * @param discountCode the discount code to check access for
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToDiscountCode(User user, DiscountCodeDto discountCode) {
        String roleName = user.getRole().getName();

        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;

            case "COMPANY_USER":
                return user.getCompany() != null &&
                       discountCode.companyId != null &&
                       user.getCompany().getId().equals(discountCode.companyId);

            case "STORE_USER":
                return user.getStore() != null &&
                       user.getStore().getCompany() != null &&
                       discountCode.companyId != null &&
                       user.getStore().getCompany().getId().equals(discountCode.companyId);

            default:
                return false;
        }
    }

    /**
     * Block or unblock a discount code by code
     * @param code the discount code
     * @param block true to block, false to unblock
     * @return the updated discount code DTO
     * @throws ValidationException if discount code not found or already in the requested state
     */
    @Transactional
    public DiscountCodeDto blockDiscountCode(String code, boolean block) {
        logger.info("{} discount code: {}", block ? "Blocking" : "Unblocking", code);
        
        User currentUser = securityContextService.getCurrentUserOrThrow();
        
        DiscountCode discountCode = codeRepository.findByCode(code.toUpperCase());
        if (discountCode == null) {
            logger.warn("Discount code not found: {}", code);
            throw new ValidationException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND, "Discount code not found: " + code);
        }

        if (block) {
            if (discountCode.isBlocked()) {
                logger.warn("Discount code {} is already blocked", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_INVALID, "Discount code is already blocked");
            }
            discountCode.setBlocked(true);
            discountCode.setBlockedBy(currentUser);
            discountCode.setBlockedDate(LocalDateTime.now());
            logger.info("Discount code {} blocked by user {}", code, currentUser.getUsername());
        } else {
            if (!discountCode.isBlocked()) {
                logger.warn("Discount code {} is not blocked", code);
                throw new ValidationException(ErrorCodes.DISCOUNT_CODE_INVALID, "Discount code is not blocked");
            }
            discountCode.setBlocked(false);
            discountCode.setBlockedBy(null);
            discountCode.setBlockedDate(null);
            logger.info("Discount code {} unblocked by user {}", code, currentUser.getUsername());
        }

        DiscountCode savedDiscountCode = codeRepository.save(discountCode);
        return mapper.getFrom(savedDiscountCode);
    }

}
