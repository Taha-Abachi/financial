package com.pars.financial.service;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.GiftCardIssueRequest;
import com.pars.financial.dto.GiftCardReportDto;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.Store;
import com.pars.financial.exception.GiftCardNotFoundException;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.GiftCardMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.GiftCardRepository;
import com.pars.financial.repository.GiftCardTransactionRepository;
import com.pars.financial.repository.ItemCategoryRepository;
import com.pars.financial.repository.StoreRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GiftCardService {

    @Value("${spring.application.giftcard.len}")
    private int giftCardSerialNoLength;

    private static final Logger logger = LoggerFactory.getLogger(GiftCardService.class);

    final GiftCardRepository giftCardRepository;
    final GiftCardMapper giftCardMapper;
    final StoreRepository storeRepository;
    final CompanyRepository companyRepository;
    final ItemCategoryRepository itemCategoryRepository;
    final GiftCardTransactionRepository giftCardTransactionRepository;

    public GiftCardService(GiftCardRepository giftCardRepository, GiftCardMapper giftCardMapper, StoreRepository storeRepository, CompanyRepository companyRepository, ItemCategoryRepository itemCategoryRepository, GiftCardTransactionRepository giftCardTransactionRepository) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardMapper = giftCardMapper;
        this.storeRepository = storeRepository;
        this.companyRepository = companyRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
    }

    private void validateRealAmount(long realAmount) {
        if (realAmount <= 0) {
            logger.error("Invalid real amount: {}", realAmount);
            throw new ValidationException(ErrorCodes.INVALID_AMOUNT, "Real amount must be greater than 0");
        }
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod, long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds) {
        return issueGiftCard(realAmount, amount, validityPeriod, companyId, storeLimited, allowedStoreIds, itemCategoryLimited, allowedItemCategoryIds, null);
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod, long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, com.pars.financial.entity.Batch batch) {
        logger.debug("Issuing new gift card with realAmount: {}, amount: {}, validityPeriod: {}, storeLimited: {}, itemCategoryLimited: {}", realAmount, amount, validityPeriod, storeLimited, itemCategoryLimited);
        validateRealAmount(realAmount);
        var company = companyRepository.findById(companyId);
        if(company.isEmpty()) {
            logger.error("Company not found while Issuing new gift card with realAmount: {}, amount: {}, validityPeriod: {}", realAmount, amount, validityPeriod);
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }
        var gc = new GiftCard();
        gc.setCompany(company.get());
        gc.setIdentifier(ThreadLocalRandom.current().nextLong(10000000, 100000000));
//        gc.setIdentifier(Long.parseLong(RandomStringGenerator.generateRandomNumericString(8)));
        gc.setSerialNo("GC" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(giftCardSerialNoLength - 2));
        gc.setInitialAmount(amount);
        gc.setRealAmount(realAmount);
        gc.setBalance(amount);
        gc.setIssueDate(LocalDate.now());
        gc.setExpiryDate(LocalDate.now().plusDays(validityPeriod));
        
        // Set store limitations
        gc.setStoreLimited(storeLimited);
        if (storeLimited && allowedStoreIds != null && !allowedStoreIds.isEmpty()) {
            java.util.Set<com.pars.financial.entity.Store> stores = new java.util.HashSet<>();
            for (Long storeId : allowedStoreIds) {
                var storeOpt = storeRepository.findById(storeId);
                if (storeOpt.isPresent()) {
                    stores.add(storeOpt.get());
                } else {
                    logger.warn("Store not found with id: {} while assigning to gift card", storeId);
                }
            }
            gc.setAllowedStores(stores);
        }
        
        // Set item category limitations
        gc.setItemCategoryLimited(itemCategoryLimited);
        if (itemCategoryLimited && allowedItemCategoryIds != null && !allowedItemCategoryIds.isEmpty()) {
            java.util.Set<com.pars.financial.entity.ItemCategory> itemCategories = new java.util.HashSet<>();
            for (Long itemCategoryId : allowedItemCategoryIds) {
                var itemCategoryOpt = itemCategoryRepository.findById(itemCategoryId);
                if (itemCategoryOpt.isPresent()) {
                    itemCategories.add(itemCategoryOpt.get());
                } else {
                    logger.warn("Item category not found with id: {} while assigning to gift card", itemCategoryId);
                }
            }
            gc.setAllowedItemCategories(itemCategories);
        }
        
        // Set batch reference if provided
        if (batch != null) {
            gc.setBatch(batch);
        }
        
        logger.debug("Created gift card with serialNo: {}", gc.getSerialNo());
        return gc;
    }

    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCards(int page, int size) {
        logger.debug("Fetching gift cards with pagination - page: {}, size: {}", page, size);
        
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
        Page<GiftCard> giftCardPage = giftCardRepository.findAll(pageable);
        
        List<GiftCardDto> giftCards = giftCardMapper.getFrom(giftCardPage.getContent());
        
        return new PagedResponse<>(
            giftCards,
            giftCardPage.getNumber(),
            giftCardPage.getSize(),
            giftCardPage.getTotalElements(),
            giftCardPage.getTotalPages()
        );
    }

    public GiftCardDto getGiftCard(String serialNo) {
        logger.debug("Fetching gift card by serialNo: {}", serialNo);
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if (gc == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }
        return giftCardMapper.getFrom(gc);
    }

    public GiftCardDto getGiftCard(Long identifier) {
        logger.debug("Fetching gift card by identifier: {}", identifier);
        var gc = giftCardRepository.findByIdentifier(identifier);
        if (gc == null) {
            logger.warn("Gift card not found with identifier: {}", identifier);
            throw new GiftCardNotFoundException("Gift Card Not Found with identifier: " + identifier);
        }
        return giftCardMapper.getFrom(gc);
    }

    public GiftCardDto generateGiftCard(long realAmount, long amount, long validityPeriod, long companyId) {
        logger.info("Generating new gift card with realAmount: {}, amount: {}, validityPeriod: {}", realAmount, amount, validityPeriod);
        var giftCard = issueGiftCard(realAmount, amount, validityPeriod, companyId, false, new ArrayList<>(), false, new ArrayList<>());
        var savedCard = giftCardRepository.save(giftCard);
        logger.info("Generated gift card with serialNo: {}", savedCard.getSerialNo());
        return giftCardMapper.getFrom(savedCard);
    }

    public GiftCardDto generateGiftCard(GiftCardIssueRequest request) {
        logger.info("Generating new gift card with request: {}", request);
        var giftCard = issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                   request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(), 
                                   request.isItemCategoryLimited(), request.getAllowedItemCategoryIds());
        var savedCard = giftCardRepository.save(giftCard);
        logger.info("Generated gift card with serialNo: {}", savedCard.getSerialNo());
        return giftCardMapper.getFrom(savedCard);
    }

    public List<GiftCardDto> generateGiftCards(long realAmount, long amount, long validityPeriod, Long companyId, int count) {
        logger.info("Generating {} gift cards with realAmount: {}, amount: {}, validityPeriod: {}", count, realAmount, amount, validityPeriod);
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < count; i++) {
            ls.add(issueGiftCard(realAmount, amount, validityPeriod, companyId, false, new ArrayList<>(), false, new ArrayList<>()));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully", count);
        return giftCardMapper.getFrom(savedCards);
    }

    public List<GiftCardDto> generateGiftCards(GiftCardIssueRequest request) {
        logger.info("Generating {} gift cards with request: {}", request.getCount(), request);
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < request.getCount(); i++) {
            ls.add(issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(), 
                                request.isItemCategoryLimited(), request.getAllowedItemCategoryIds()));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully", request.getCount());
        return giftCardMapper.getFrom(savedCards);
    }

    public List<GiftCardDto> generateGiftCards(GiftCardIssueRequest request, com.pars.financial.entity.Batch batch) {
        logger.info("Generating {} gift cards with request: {} for batch: {}", request.getCount(), request, batch.getBatchNumber());
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < request.getCount(); i++) {
            ls.add(issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(), 
                                request.isItemCategoryLimited(), request.getAllowedItemCategoryIds(), batch));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully for batch: {}", request.getCount(), batch.getBatchNumber());
        return giftCardMapper.getFrom(savedCards);
    }

    @Transactional
    public void limitToStores(String serialNo, List<Long> storeIds) {
        logger.info("Limiting gift card {} to stores: {}", serialNo, storeIds);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }

        if (storeIds == null || storeIds.isEmpty()) {
            logger.debug("Removing store limitations for gift card: {}", serialNo);
            giftCard.setStoreLimited(false);
            giftCard.setAllowedStores(new HashSet<>());
        } else {
            Set<Store> stores = new HashSet<>();
            for (Long storeId : storeIds) {
                var store = storeRepository.findById(storeId)
                    .orElseThrow(() -> {
                        logger.warn("Store not found with id: {}", storeId);
                        return new ValidationException(ErrorCodes.STORE_NOT_FOUND, "Store not found with id: " + storeId);
                    });
                stores.add(store);
            }
            giftCard.setStoreLimited(true);
            giftCard.setAllowedStores(stores);
            logger.debug("Limited gift card {} to {} stores", serialNo, stores.size());
        }
        giftCardRepository.save(giftCard);
        logger.info("Successfully updated store limitations for gift card: {}", serialNo);
    }

    @Transactional
    public void removeStoreLimitation(String serialNo) {
        logger.info("Removing store limitations for gift card: {}", serialNo);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }
        giftCard.setStoreLimited(false);
        giftCard.setAllowedStores(new HashSet<>());
        giftCardRepository.save(giftCard);
        logger.info("Successfully removed store limitations for gift card: {}", serialNo);
    }

    @Transactional
    public void limitToItemCategories(String serialNo, List<Long> itemCategoryIds) {
        logger.info("Limiting gift card {} to item categories: {}", serialNo, itemCategoryIds);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }

        if (itemCategoryIds == null || itemCategoryIds.isEmpty()) {
            logger.debug("Removing item category limitations for gift card: {}", serialNo);
            giftCard.setItemCategoryLimited(false);
            giftCard.setAllowedItemCategories(new HashSet<>());
        } else {
            Set<com.pars.financial.entity.ItemCategory> itemCategories = new HashSet<>();
            for (Long itemCategoryId : itemCategoryIds) {
                var itemCategory = itemCategoryRepository.findById(itemCategoryId)
                    .orElseThrow(() -> {
                        logger.warn("Item category not found with id: {}", itemCategoryId);
                        return new ValidationException(ErrorCodes.ITEM_CATEGORY_NOT_FOUND, "Item category not found with id: " + itemCategoryId);
                    });
                itemCategories.add(itemCategory);
            }
            giftCard.setItemCategoryLimited(true);
            giftCard.setAllowedItemCategories(itemCategories);
            logger.debug("Limited gift card {} to {} item categories", serialNo, itemCategories.size());
        }
        giftCardRepository.save(giftCard);
        logger.info("Successfully updated item category limitations for gift card: {}", serialNo);
    }

    @Transactional
    public void removeItemCategoryLimitation(String serialNo) {
        logger.info("Removing item category limitations for gift card: {}", serialNo);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }
        giftCard.setItemCategoryLimited(false);
        giftCard.setAllowedItemCategories(new HashSet<>());
        giftCardRepository.save(giftCard);
        logger.info("Successfully removed item category limitations for gift card: {}", serialNo);
    }

    /**
     * Get all gift cards for a specific company
     * @param companyId the company ID
     * @return list of gift card DTOs
     */
    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCardsByCompany(Long companyId, int page, int size) {
        logger.info("Fetching gift cards for company: {} with pagination - page: {}, size: {}", companyId, page, size);
        
        var company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            logger.warn("Company not found with ID: {}", companyId);
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }
        
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
        Page<GiftCard> giftCardPage = giftCardRepository.findByCompany(company.get(), pageable);
        
        List<GiftCardDto> giftCards = giftCardMapper.getFrom(giftCardPage.getContent());
        
        return new PagedResponse<>(
            giftCards,
            giftCardPage.getNumber(),
            giftCardPage.getSize(),
            giftCardPage.getTotalElements(),
            giftCardPage.getTotalPages()
        );
    }

    /**
     * Assign a company to a gift card
     * @param serialNo the gift card serial number
     * @param companyId the company ID
     * @return the updated gift card DTO
     */
    @Transactional
    public GiftCardDto assignCompanyToGiftCard(String serialNo, Long companyId) {
        logger.info("Assigning company {} to gift card: {}", companyId, serialNo);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }

        var company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            logger.warn("Company not found with ID: {}", companyId);
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }

        giftCard.setCompany(company.get());
        var savedGiftCard = giftCardRepository.save(giftCard);
        logger.info("Successfully assigned company {} to gift card: {}", companyId, serialNo);
        return giftCardMapper.getFrom(savedGiftCard);
    }

    /**
     * Remove company assignment from a gift card
     * @param serialNo the gift card serial number
     * @return the updated gift card DTO
     */
    @Transactional
    public GiftCardDto removeCompanyFromGiftCard(String serialNo) {
        logger.info("Removing company assignment from gift card: {}", serialNo);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }

        giftCard.setCompany(null);
        var savedGiftCard = giftCardRepository.save(giftCard);
        logger.info("Successfully removed company assignment from gift card: {}", serialNo);
        return giftCardMapper.getFrom(savedGiftCard);
    }

    /**
     * Generate comprehensive gift card report for all gift cards
     * @return GiftCardReportDto containing all statistics
     */
    public GiftCardReportDto generateGiftCardReport() {
        logger.info("Generating comprehensive gift card report");
        
        try {
            // Get basic statistics
            Long totalCount = giftCardRepository.countAllGiftCards();
            Long totalBalance = giftCardRepository.sumTotalBalance();
            Long totalInitialAmount = giftCardRepository.sumTotalInitialAmount();
            Long activeCount = giftCardRepository.countActiveGiftCards();
            Long blockedCount = giftCardRepository.countBlockedGiftCards();
            Long expiredCount = giftCardRepository.countExpiredGiftCards(LocalDate.now());
            Long usedCount = giftCardRepository.countUsedGiftCards();
            Double averageBalance = giftCardRepository.getAverageBalance();
            Double averageInitialAmount = giftCardRepository.getAverageInitialAmount();
            
            // Get debit transaction statistics
            Long totalDebitTransactions = giftCardTransactionRepository.countDebitTransactions();
            Long totalDebitAmount = giftCardTransactionRepository.sumDebitAmount();
            
            GiftCardReportDto report = new GiftCardReportDto(
                totalCount, totalBalance, totalInitialAmount,
                totalDebitTransactions, totalDebitAmount,
                activeCount, blockedCount, expiredCount, usedCount,
                averageBalance, averageInitialAmount
            );
            
            logger.info("Successfully generated gift card report: totalCount={}, totalBalance={}, totalInitialAmount={}, totalDebitTransactions={}", 
                       totalCount, totalBalance, totalInitialAmount, totalDebitTransactions);
            
            return report;
            
        } catch (Exception e) {
            logger.error("Error generating gift card report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate gift card report", e);
        }
    }

    /**
     * Generate gift card report for a specific company
     * @param companyId the company ID
     * @return GiftCardReportDto containing company-specific statistics
     */
    public GiftCardReportDto generateGiftCardReportByCompany(Long companyId) {
        logger.info("Generating gift card report for company: {}", companyId);
        
        try {
            // Validate company exists
            if (!companyRepository.existsById(companyId)) {
                throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
            }
            
            // Get company-specific statistics
            Long totalCount = giftCardRepository.countGiftCardsByCompany(companyId);
            Long totalBalance = giftCardRepository.sumBalanceByCompany(companyId);
            Long totalInitialAmount = giftCardRepository.sumInitialAmountByCompany(companyId);
            Long activeCount = giftCardRepository.countActiveGiftCardsByCompany(companyId);
            Long blockedCount = giftCardRepository.countBlockedGiftCardsByCompany(companyId);
            Long expiredCount = giftCardRepository.countExpiredGiftCardsByCompany(companyId, LocalDate.now());
            Long usedCount = giftCardRepository.countUsedGiftCardsByCompany(companyId);
            Double averageBalance = giftCardRepository.getAverageBalanceByCompany(companyId);
            Double averageInitialAmount = giftCardRepository.getAverageInitialAmountByCompany(companyId);
            
            // Get company-specific debit transaction statistics
            Long totalDebitTransactions = giftCardTransactionRepository.countDebitTransactionsByCompany(companyId);
            Long totalDebitAmount = giftCardTransactionRepository.sumDebitAmountByCompany(companyId);
            
            GiftCardReportDto report = new GiftCardReportDto(
                totalCount, totalBalance, totalInitialAmount,
                totalDebitTransactions, totalDebitAmount,
                activeCount, blockedCount, expiredCount, usedCount,
                averageBalance, averageInitialAmount
            );
            
            logger.info("Successfully generated gift card report for company {}: totalCount={}, totalBalance={}, totalInitialAmount={}, totalDebitTransactions={}", 
                       companyId, totalCount, totalBalance, totalInitialAmount, totalDebitTransactions);
            
            return report;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating gift card report for company {}: {}", companyId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate gift card report for company", e);
        }
    }
}


