package com.pars.financial.service;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.entity.DiscountCode;
import com.pars.financial.enums.DiscountType;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.DiscountCodeMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.DiscountCodeRepository;
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

    public DiscountCodeService(DiscountCodeRepository codeRepository, DiscountCodeMapper mapper, CompanyRepository companyRepository, StoreRepository storeRepository) {
        this.codeRepository = codeRepository;
        this.mapper = mapper;
        this.companyRepository = companyRepository;
        this.storeRepository = storeRepository;
    }

    private DiscountCode issueDiscountCode(int percentage, long validityPeriod, long maxDiscountAmount, long minimumBillAmount, int usageLimit, long constantDiscountAmount, DiscountType discountType, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds) {
        logger.debug("Issuing new discount code with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}", 
            percentage, validityPeriod, maxDiscountAmount, minimumBillAmount, usageLimit, constantDiscountAmount, discountType, companyId, storeLimited, allowedStoreIds);
        var code = new DiscountCode();
        code.setIssueDate(LocalDateTime.now());
        code.setExpiryDate(LocalDate.now().plusDays(validityPeriod));
        code.setCode("DC" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(discountCodeLength - 2));
        code.setSerialNo(ThreadLocalRandom.current().nextLong(10000000, 100000000));
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
        logger.info("Generating new discount code with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}", 
            dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds);
        var discountCode = issueDiscountCode(dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount, dto.minimumBillAmount, dto.usageLimit, dto.constantDiscountAmount, dto.discountType, dto.companyId, (long) dto.allowedStoreIds.size() > 0, dto.allowedStoreIds);
        var savedCode = codeRepository.save(discountCode);
        logger.info("Generated discount code: {}", savedCode.getCode());
        return mapper.getFrom(savedCode);
    }

    public List<DiscountCodeDto> generateList(DiscountCodeIssueRequest request) {
        request.storeLimited = !request.allowedStoreIds.isEmpty();
        logger.info("Generating {} discount codes with percentage: {}, validityPeriod: {}, maxDiscountAmount: {}, minimumBillAmount: {}, usageLimit: {}, constantDiscountAmount: {}, discountType: {}, companyId: {}, storeLimited: {}, allowedStoreIds: {}", 
            request.count, request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds);
        var ls = new ArrayList<DiscountCode>();
        for (var i = 0; i < request.count; i++) {
            ls.add(issueDiscountCode(request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount, request.minimumBillAmount, request.usageLimit, request.constantDiscountAmount, request.discountType, request.companyId, request.storeLimited, request.allowedStoreIds));
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
}
