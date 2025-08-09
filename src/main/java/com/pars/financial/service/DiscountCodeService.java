package com.pars.financial.service;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.entity.DiscountCode;
import com.pars.financial.enums.DiscountType;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.DiscountCodeMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.repository.ItemCategoryRepository;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.utils.RandomStringGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public DiscountCodeService(DiscountCodeRepository codeRepository, DiscountCodeMapper mapper, CompanyRepository companyRepository, StoreRepository storeRepository, ItemCategoryRepository itemCategoryRepository) {
        this.codeRepository = codeRepository;
        this.mapper = mapper;
        this.companyRepository = companyRepository;
        this.storeRepository = storeRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }

    private DiscountCode issueDiscountCode(int percentage, long validityPeriod, long maxDiscountAmount, long minimumBillAmount, int usageLimit, long constantDiscountAmount, DiscountType discountType, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String customCode, Long customSerialNo) {
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
                throw new ValidationException("Company not found", null, -134);
            }
            code.setCompany(company.get());
            logger.debug("Assigned discount code to company: {}", company.get().getName());
        }
        logger.debug("Created discount code: {}", code.getCode());
        return code;
    }

    public DiscountCodeDto generate(DiscountCodeIssueRequest dto) {
        logger.info("Generating new discount code with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}, itemCategoryLimited: {}, allowedItemCategoryIds: {}, customCode: {}, customSerialNo: {}", 
            dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds, dto.itemCategoryLimited, dto.allowedItemCategoryIds, dto.code, dto.serialNo);
        
        // Check if both code and serialNo are provided simultaneously and count is 1
        boolean hasCustomCode = dto.code != null && !dto.code.trim().isEmpty();
        boolean hasCustomSerialNo = dto.serialNo != null;
        boolean isSingleCode = dto.count <= 1;
        
        if (hasCustomCode && hasCustomSerialNo && isSingleCode) {
            // Check if the provided code and serialNo are not already in the database
            if (codeRepository.existsByCode(dto.code)) {
                logger.warn("Discount code already exists: {}", dto.code);
                throw new ValidationException("Discount code already exists", null, -150);
            }
            if (codeRepository.existsBySerialNo(dto.serialNo)) {
                logger.warn("Serial number already exists: {}", dto.serialNo);
                throw new ValidationException("Serial number already exists", null, -151);
            }
            logger.info("Using provided custom code: {} and serial number: {}", dto.code, dto.serialNo);
        } else if (hasCustomCode || hasCustomSerialNo) {
            // If only one of them is provided, or count is not 1, ignore custom values and generate random
            logger.warn("Both code and serialNo must be provided simultaneously and count must be 1 to use custom values. Generating random values instead.");
            dto.code = null;
            dto.serialNo = null;
        }
        
        var discountCode = issueDiscountCode(dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds, dto.itemCategoryLimited, dto.allowedItemCategoryIds, dto.code, dto.serialNo);
        var savedCode = codeRepository.save(discountCode);
        logger.info("Generated discount code: {}", savedCode.getCode());
        return mapper.getFrom(savedCode);
    }

    public List<DiscountCodeDto> generateList(DiscountCodeIssueRequest request) {
        request.storeLimited = !request.allowedStoreIds.isEmpty();
        request.itemCategoryLimited = !request.allowedItemCategoryIds.isEmpty();
        logger.info("Generating {} discount codes with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}, itemCategoryLimited: {}, allowedItemCategoryIds: {}, customCode: {}, customSerialNo: {}", 
            request.count, request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds, request.itemCategoryLimited, request.allowedItemCategoryIds, request.code, request.serialNo);
        
        // Check if both code and serialNo are provided simultaneously and count is 1
        boolean hasCustomCode = request.code != null && !request.code.trim().isEmpty();
        boolean hasCustomSerialNo = request.serialNo != null;
        boolean isSingleCode = request.count == 1;
        
        if (hasCustomCode && hasCustomSerialNo && isSingleCode) {
            // Check if the provided code and serialNo are not already in the database
            if (codeRepository.existsByCode(request.code)) {
                logger.warn("Discount code already exists: {}", request.code);
                throw new ValidationException("Discount code already exists", null, -150);
            }
            if (codeRepository.existsBySerialNo(request.serialNo)) {
                logger.warn("Serial number already exists: {}", request.serialNo);
                throw new ValidationException("Serial number already exists", null, -151);
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
            var discountCode = issueDiscountCode(request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds, request.itemCategoryLimited, request.allowedItemCategoryIds, request.code, request.serialNo);
            ls.add(discountCode);
        }
        var savedCodes = codeRepository.saveAll(ls);
        logger.info("Generated {} discount codes successfully", request.count);
        return mapper.getFrom(savedCodes);
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
            throw new ValidationException("Discount code not found", null, -118);
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
                        return new ValidationException("Store not found with id: " + storeId, null, -116);
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
            throw new ValidationException("Discount code not found", null, -118);
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
            throw new ValidationException("Discount code not found", null, -118);
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
                        return new ValidationException("Item category not found with id: " + itemCategoryId, null, -117);
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
            throw new ValidationException("Discount code not found", null, -118);
        }
        discountCode.setItemCategoryLimited(false);
        discountCode.setAllowedItemCategories(new java.util.HashSet<>());
        codeRepository.save(discountCode);
        logger.info("Successfully removed item category limitations for discount code: {}", code);
    }
}
