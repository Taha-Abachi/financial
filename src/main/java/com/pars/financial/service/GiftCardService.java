package com.pars.financial.service;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.GiftCardIssueRequest;
import com.pars.financial.dto.GiftCardReportDto;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.entity.Company;
import com.pars.financial.entity.Customer;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.enums.GiftCardType;
import com.pars.financial.exception.GiftCardNotFoundException;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.GiftCardMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.CustomerRepository;
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
import org.springframework.data.domain.Sort;
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
    final SecurityContextService securityContextService;
    final CustomerService customerService;
    final CustomerRepository customerRepository;

    public GiftCardService(GiftCardRepository giftCardRepository, GiftCardMapper giftCardMapper, StoreRepository storeRepository, CompanyRepository companyRepository, ItemCategoryRepository itemCategoryRepository, GiftCardTransactionRepository giftCardTransactionRepository, SecurityContextService securityContextService, CustomerService customerService, CustomerRepository customerRepository) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardMapper = giftCardMapper;
        this.storeRepository = storeRepository;
        this.companyRepository = companyRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.securityContextService = securityContextService;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    private void validateRealAmount(long realAmount) {
        if (realAmount <= 0) {
            logger.error("Invalid real amount: {}", realAmount);
            throw new ValidationException(ErrorCodes.INVALID_AMOUNT, "Real amount must be greater than 0");
        }
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String title) {
        return issueGiftCard(realAmount, amount, validityPeriod, companyId, storeLimited, allowedStoreIds, itemCategoryLimited, allowedItemCategoryIds, title, null, GiftCardType.PHYSICAL);
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String title, com.pars.financial.entity.Batch batch) {
        return issueGiftCard(realAmount, amount, validityPeriod, companyId, storeLimited, allowedStoreIds, itemCategoryLimited, allowedItemCategoryIds, title, batch, GiftCardType.PHYSICAL);
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod, Long companyId, boolean storeLimited, java.util.List<Long> allowedStoreIds, boolean itemCategoryLimited, java.util.List<Long> allowedItemCategoryIds, String title, com.pars.financial.entity.Batch batch, GiftCardType type) {
        logger.debug("Issuing new gift card with realAmount: {}, amount: {}, validityPeriod: {}, storeLimited: {}, itemCategoryLimited: {}", realAmount, amount, validityPeriod, storeLimited, itemCategoryLimited);
        validateRealAmount(realAmount);
        
        // Set company with priority: batch company > request companyId > current user's company
        Company companyToAssign = null;
        
        // Priority 1: If batch is provided, use batch's company
        if (batch != null && batch.getCompany() != null) {
            companyToAssign = batch.getCompany();
            logger.debug("Using batch company: {} for gift card", companyToAssign.getName());
        }
        // Priority 2: If companyId is provided in request, use it
        else if (companyId != null) {
            var companyOpt = companyRepository.findById(companyId);
            if (companyOpt.isEmpty()) {
                logger.warn("Company not found with ID: {}", companyId);
                throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND, "Company not found with ID: " + companyId);
            }
            companyToAssign = companyOpt.get();
            logger.debug("Using provided companyId: {} for gift card", companyToAssign.getName());
        }
        // Priority 3: Try to get from current user (for COMPANY_USER/STORE_USER)
        else {
            try {
                User currentUser = securityContextService.getCurrentUserOrThrow();
                String roleName = currentUser.getRole().getName();
                
                if ("COMPANY_USER".equals(roleName)) {
                    if (currentUser.getCompany() != null) {
                        companyToAssign = currentUser.getCompany();
                        logger.debug("Using current user's company: {} for gift card", companyToAssign.getName());
                    } else {
                        logger.warn("COMPANY_USER {} has no company assigned", currentUser.getUsername());
                    }
                } else if ("STORE_USER".equals(roleName)) {
                    if (currentUser.getStore() != null && currentUser.getStore().getCompany() != null) {
                        companyToAssign = currentUser.getStore().getCompany();
                        logger.debug("Using current user's store's company: {} for gift card", companyToAssign.getName());
                    } else {
                        logger.warn("STORE_USER {} has no store or company assigned", currentUser.getUsername());
                    }
                }
            } catch (IllegalStateException e) {
                // User not authenticated - will throw error below
                logger.debug("No authenticated user found, cannot determine company from user");
            }
        }
        
        // Ensure company is set - throw error if still null
        if (companyToAssign == null) {
            logger.error("Cannot create gift card without company_id. Batch company, request companyId, and user company are all null or unavailable.");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Company ID is required. Please provide companyId in the request or ensure the batch has a company assigned.");
        }
        
        var gc = new GiftCard();
        gc.setCompany(companyToAssign);
        gc.setIdentifier(ThreadLocalRandom.current().nextLong(10000000, 100000000));
//        gc.setIdentifier(Long.parseLong(RandomStringGenerator.generateRandomNumericString(8)));
        gc.setSerialNo("GC" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(giftCardSerialNoLength - 2));
        gc.setTitle(title);
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
        
        // Set type (default to PHYSICAL if null)
        gc.setType(type != null ? type : GiftCardType.PHYSICAL);
        
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

    public GiftCardDto generateGiftCard(long realAmount, long amount, long validityPeriod, Long companyId) {
        logger.info("Generating new gift card with realAmount: {}, amount: {}, validityPeriod: {}", realAmount, amount, validityPeriod);
        var giftCard = issueGiftCard(realAmount, amount, validityPeriod, companyId, false, new ArrayList<>(), false, new ArrayList<>(), "");
        var savedCard = giftCardRepository.save(giftCard);
        logger.info("Generated gift card with serialNo: {}", savedCard.getSerialNo());
        return giftCardMapper.getFrom(savedCard);
    }

    public GiftCardDto generateGiftCard(GiftCardIssueRequest request) {
        logger.info("Generating new gift card with request: {}", request);
        var giftCard = issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                   request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(), 
                                   request.isItemCategoryLimited(), request.getAllowedItemCategoryIds(), 
                                   request.getTitle() != null ? request.getTitle() : "", null, 
                                   request.getType() != null ? request.getType() : GiftCardType.PHYSICAL);
        var savedCard = giftCardRepository.save(giftCard);
        logger.info("Generated gift card with serialNo: {}", savedCard.getSerialNo());
        return giftCardMapper.getFrom(savedCard);
    }

    public List<GiftCardDto> generateGiftCards(long realAmount, long amount, long validityPeriod, Long companyId, int count) {
        logger.info("Generating {} gift cards with realAmount: {}, amount: {}, validityPeriod: {}", count, realAmount, amount, validityPeriod);
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < count; i++) {
            ls.add(issueGiftCard(realAmount, amount, validityPeriod, companyId, false, new ArrayList<>(), false, new ArrayList<>(), ""));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully", count);
        return giftCardMapper.getFrom(savedCards);
    }

    public List<GiftCardDto> generateGiftCards(GiftCardIssueRequest request) {
        logger.info("Generating {} gift cards with request: {}", request.getCount(), request);
        var ls = new ArrayList<GiftCard>();
        GiftCardType cardType = request.getType() != null ? request.getType() : GiftCardType.PHYSICAL;
        for (var i = 0; i < request.getCount(); i++) {
            ls.add(issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(), 
                                request.isItemCategoryLimited(), request.getAllowedItemCategoryIds(), 
                                request.getTitle() != null ? request.getTitle() : "", null, cardType));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully", request.getCount());
        return giftCardMapper.getFrom(savedCards);
    }

    public List<GiftCardDto> generateGiftCards(GiftCardIssueRequest request, com.pars.financial.entity.Batch batch) {
        logger.info("Generating {} gift cards with request: {} for batch: {}", request.getCount(), request, batch.getBatchNumber());
        var ls = new ArrayList<GiftCard>();
        GiftCardType cardType = request.getType() != null ? request.getType() : GiftCardType.PHYSICAL;
        for (var i = 0; i < request.getCount(); i++) {
            ls.add(issueGiftCard(request.getRealAmount(), request.getBalance(), request.getRemainingValidityPeriod(), 
                                request.getCompanyId(), request.isStoreLimited(), request.getAllowedStoreIds(),
                                request.isItemCategoryLimited(), request.getAllowedItemCategoryIds(), 
                                request.getTitle() != null ? request.getTitle() : "", batch, cardType));
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

    /**
     * Get gift cards based on user role and permissions
     * - SUPERADMIN/ADMIN: All gift cards
     * - COMPANY_USER: Gift cards of their company
     * - STORE_USER: Gift cards of their store
     * - API_USER: All gift cards (if system-wide access needed)
     */
    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCardsForUser(User user, int page, int size) {
        logger.debug("Fetching gift cards for user: {} with role: {} - page: {}, size: {}", 
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
        Page<GiftCard> giftCardPage;
        
        // Role-based data filtering
        switch (user.getRole().getName()) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // Return all gift cards
                logger.debug("User {} has admin access - returning all gift cards", user.getUsername());
                giftCardPage = giftCardRepository.findAll(pageable);
                break;
                
            case "COMPANY_USER":
                // Return gift cards of user's company
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    giftCardPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning gift cards for company: {}", 
                               user.getUsername(), user.getCompany().getId());
                    giftCardPage = giftCardRepository.findByCompany(user.getCompany(), pageable);
                }
                break;
                
            case "STORE_USER":
                // Return gift cards of user's store
                if (user.getStore() == null) {
                    logger.warn("STORE_USER {} has no store assigned", user.getUsername());
                    giftCardPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has store access - returning gift cards for store: {}", 
                               user.getUsername(), user.getStore().getId());
                    // For STORE_USER, we need to get gift cards that belong to their store's company
                    giftCardPage = giftCardRepository.findByCompany(user.getStore().getCompany(), pageable);
                }
                break;
                
            default:
                logger.warn("Unknown role {} for user {} - returning empty result", 
                           user.getRole().getName(), user.getUsername());
                giftCardPage = Page.empty(pageable);
                break;
        }
        
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
     * Get gift cards based on current user's role and permissions
     * - SUPERADMIN/ADMIN: All gift cards
     * - COMPANY_USER: Gift cards of their company
     * - STORE_USER: Gift cards of their store's company
     * - API_USER: All gift cards (if system-wide access needed)
     */
    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCardsForCurrentUser(int page, int size) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }
        
        return getGiftCardsForUser(currentUser, page, size);
    }

    /**
     * Get gift cards with RBAC and optional filtering for superadmin
     * - SUPERADMIN: Can filter by companyId and/or storeId
     * - COMPANY_USER: Only sees their company's gift cards
     * - STORE_USER: Only sees their store's company's gift cards
     */
    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCardsForCurrentUserWithFiltering(User user, int page, int size, Long companyId, Long storeId, String sortBy, String sortDir) {
        logger.debug("Fetching gift cards for user: {} with role: {} - page: {}, size: {}, companyId: {}, storeId: {}, sortBy: {}, sortDir: {}", 
                    user.getUsername(), user.getRole().getName(), page, size, companyId, storeId, sortBy, sortDir);
        
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
        
        // Validate and set default sort field
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "id";
        }
        
        // Validate sort direction
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDir != null && !sortDir.trim().isEmpty()) {
            try {
                direction = Sort.Direction.fromString(sortDir.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid sort direction: {}, using ASC", sortDir);
                direction = Sort.Direction.ASC;
            }
        }

        // Create Pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<GiftCard> giftCardPage;
        
        // Role-based data filtering
        switch (user.getRole().getName()) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // Superadmin can filter by company and/or store
                if (companyId != null) {
                    // Validate company exists
                    var company = companyRepository.findById(companyId);
                    if (company.isEmpty()) {
                        logger.warn("Company not found with ID: {}", companyId);
                        throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
                    }
                    
                    if (storeId != null) {
                        // Filter by both company and store
                        var store = storeRepository.findById(storeId);
                        if (store.isEmpty()) {
                            logger.warn("Store not found with ID: {}", storeId);
                            throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                        }
                        // Validate store belongs to company
                        if (!store.get().getCompany().getId().equals(companyId)) {
                            logger.warn("Store {} does not belong to company {}", storeId, companyId);
                            throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                        }
                        // For now, return company's gift cards (store filtering would require additional repository method)
                        giftCardPage = giftCardRepository.findByCompany(company.get(), pageable);
                    } else {
                        // Filter by company only
                        giftCardPage = giftCardRepository.findByCompany(company.get(), pageable);
                    }
                } else if (storeId != null) {
                    // Filter by store only
                    var store = storeRepository.findById(storeId);
                    if (store.isEmpty()) {
                        logger.warn("Store not found with ID: {}", storeId);
                        throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                    }
                    giftCardPage = giftCardRepository.findByCompany(store.get().getCompany(), pageable);
                } else {
                    // No filtering - return all gift cards
                    giftCardPage = giftCardRepository.findAll(pageable);
                }
                break;
                
            case "COMPANY_USER":
                // Return gift cards of user's company
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    giftCardPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning gift cards for company: {}", 
                               user.getUsername(), user.getCompany().getId());
                    giftCardPage = giftCardRepository.findByCompany(user.getCompany(), pageable);
                }
                break;
                
            case "STORE_USER":
                // Return gift cards of user's store's company
                if (user.getStore() == null) {
                    logger.warn("STORE_USER {} has no store assigned", user.getUsername());
                    giftCardPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has store access - returning gift cards for store's company: {}", 
                               user.getUsername(), user.getStore().getCompany().getId());
                    giftCardPage = giftCardRepository.findByCompany(user.getStore().getCompany(), pageable);
                }
                break;
                
            default:
                logger.warn("Unknown role {} for user {} - returning empty result", 
                           user.getRole().getName(), user.getUsername());
                giftCardPage = Page.empty(pageable);
                break;
        }
        
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
     * Check if user has access to a specific gift card
     */
    public boolean hasAccessToGiftCard(User user, GiftCardDto giftCard) {
        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;
                
            case "COMPANY_USER":
                return user.getCompany() != null && 
                       giftCard.companyId != null && 
                       user.getCompany().getId().equals(giftCard.companyId);
                       
            case "STORE_USER":
                return user.getStore() != null && 
                       user.getStore().getCompany() != null &&
                       giftCard.companyId != null &&
                       user.getStore().getCompany().getId().equals(giftCard.companyId);
                       
            default:
                return false;
        }
    }

    /**
     * Register a gift card to the current authenticated user
     * Sets the gift card's customer if it is not already set
     * @param serialNo the gift card serial number
     * @return the updated gift card DTO
     * @throws GiftCardNotFoundException if gift card not found
     * @throws ValidationException if gift card is already registered to a customer
     * @throws IllegalStateException if user is not authenticated
     */
    @Transactional
    public GiftCardDto registerGiftCard(String serialNo) {
        logger.info("Registering gift card with serialNo: {}", serialNo);
        
        // Get current authenticated user (throws exception if not authenticated)
        User currentUser = securityContextService.getCurrentUserOrThrow();
        
        // Validate user has phone number
        if (currentUser.getMobilePhoneNumber() == null || currentUser.getMobilePhoneNumber().trim().isEmpty()) {
            logger.error("User {} does not have a mobile phone number", currentUser.getUsername());
            throw new ValidationException(ErrorCodes.CUSTOMER_PHONE_REQUIRED, "User mobile phone number is required for gift card registration");
        }
        
        // Find gift card
        GiftCard giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }
        
        // Check if gift card is already registered to a customer
        if (giftCard.getCustomer() != null) {
            logger.warn("Gift card {} is already registered to customer {}", serialNo, giftCard.getCustomer().getId());
            throw new ValidationException(ErrorCodes.CONFLICT, "Gift card is already registered to a customer");
        }
        
        // Find or create customer based on user's phone number
        Customer customer = customerRepository.findByPrimaryPhoneNumber(currentUser.getMobilePhoneNumber());
        if (customer == null) {
            logger.info("Creating new customer for phone number: {}", currentUser.getMobilePhoneNumber());
            customer = customerService.createCustomer(currentUser.getMobilePhoneNumber());
            // Optionally set customer name from user if available
            if (customer.getName() == null && currentUser.getName() != null) {
                customer.setName(currentUser.getName());
            }
            customer = customerRepository.save(customer);
        } else {
            logger.debug("Found existing customer {} for phone number: {}", customer.getId(), currentUser.getMobilePhoneNumber());
        }
        
        // Register gift card to customer
        giftCard.setCustomer(customer);
        GiftCard savedGiftCard = giftCardRepository.save(giftCard);
        
        logger.info("Successfully registered gift card {} to customer {}", serialNo, customer.getId());
        return giftCardMapper.getFrom(savedGiftCard);
    }

    /**
     * Get gift cards for a customer with access control
     * Accessible by:
     * - API users (SUPERADMIN, ADMIN, API_USER) - can access any customer's gift cards
     * - Customer themselves - can only access their own gift cards (matched by phone number)
     * 
     * @param customerId the customer ID (optional - if null, uses current user's customer)
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated list of gift cards
     * @throws ValidationException if access denied or customer not found
     */
    @Transactional(readOnly = true)
    public PagedResponse<GiftCardDto> getGiftCardsByCustomer(Long customerId, int page, int size) {
        logger.info("Fetching gift cards for customer: {} - page: {}, size: {}", customerId, page, size);
        
        // Get current authenticated user
        User currentUser = securityContextService.getCurrentUserOrThrow();
        
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
        
        Customer targetCustomer;
        
        // Determine target customer
        if (customerId != null) {
            // Customer ID provided - need to check access
            var customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                logger.warn("Customer not found with ID: {}", customerId);
                throw new ValidationException(ErrorCodes.CUSTOMER_NOT_FOUND, "Customer not found");
            }
            targetCustomer = customerOpt.get();
            
            // Check access control
            String roleName = currentUser.getRole().getName();
            boolean hasAccess = false;
            
            // API users have full access
            if ("SUPERADMIN".equals(roleName) || "ADMIN".equals(roleName) || "API_USER".equals(roleName)) {
                hasAccess = true;
                logger.debug("API user {} accessing customer {} gift cards", currentUser.getUsername(), customerId);
            } 
            // Regular users can only access their own customer record
            else if (currentUser.getMobilePhoneNumber() != null && 
                     targetCustomer.getPrimaryPhoneNumber() != null &&
                     currentUser.getMobilePhoneNumber().equals(targetCustomer.getPrimaryPhoneNumber())) {
                hasAccess = true;
                logger.debug("Customer {} accessing their own gift cards", customerId);
            }
            
            if (!hasAccess) {
                logger.warn("User {} attempted to access customer {} gift cards without permission", 
                           currentUser.getUsername(), customerId);
                throw new ValidationException(ErrorCodes.FORBIDDEN, "Access denied to this customer's gift cards");
            }
        } else {
            // No customer ID provided - use current user's customer
            if (currentUser.getMobilePhoneNumber() == null || currentUser.getMobilePhoneNumber().trim().isEmpty()) {
                logger.error("User {} does not have a mobile phone number", currentUser.getUsername());
                throw new ValidationException(ErrorCodes.CUSTOMER_PHONE_REQUIRED, "User mobile phone number is required");
            }
            
            targetCustomer = customerRepository.findByPrimaryPhoneNumber(currentUser.getMobilePhoneNumber());
            if (targetCustomer == null) {
                logger.warn("Customer not found for user {} with phone number: {}", 
                          currentUser.getUsername(), currentUser.getMobilePhoneNumber());
                throw new ValidationException(ErrorCodes.CUSTOMER_NOT_FOUND, "Customer not found for current user");
            }
            logger.debug("Using current user's customer: {}", targetCustomer.getId());
        }
        
        // Fetch gift cards for the customer
        Pageable pageable = PageRequest.of(page, size);
        Page<GiftCard> giftCardPage = giftCardRepository.findByCustomer(targetCustomer, pageable);
        
        List<GiftCardDto> giftCards = giftCardMapper.getFrom(giftCardPage.getContent());
        
        logger.info("Found {} gift cards for customer {}", giftCardPage.getTotalElements(), targetCustomer.getId());
        
        return new PagedResponse<>(
            giftCards,
            giftCardPage.getNumber(),
            giftCardPage.getSize(),
            giftCardPage.getTotalElements(),
            giftCardPage.getTotalPages()
        );
    }

    /**
     * Block or unblock a gift card by serial number
     * @param serialNo the serial number of the gift card
     * @param block true to block, false to unblock
     * @return the updated gift card DTO
     * @throws GiftCardNotFoundException if gift card not found
     */
    @Transactional
    public GiftCardDto blockGiftCard(String serialNo, boolean block) {
        logger.info("{} gift card with serialNo: {}", block ? "Blocking" : "Unblocking", serialNo);
        
        User currentUser = securityContextService.getCurrentUserOrThrow();
        
        GiftCard giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }

        // RBAC check: COMPANY_USER and STORE_USER can only block/unblock gift cards from their company
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("SUPERADMIN") && !roleName.equals("ADMIN") && !roleName.equals("API_USER")) {
            GiftCardDto dto = giftCardMapper.getFrom(giftCard);
            if (!hasAccessToGiftCard(currentUser, dto)) {
                logger.warn("User {} with role {} does not have access to block/unblock gift card {}", 
                           currentUser.getUsername(), roleName, serialNo);
                throw new ValidationException(ErrorCodes.FORBIDDEN, "You do not have access to block/unblock this gift card");
            }
        }

        if (block) {
            if (giftCard.isBlocked()) {
                logger.warn("Gift card {} is already blocked", serialNo);
                throw new ValidationException(ErrorCodes.GIFT_CARD_INVALID, "Gift card is already blocked");
            }
            giftCard.setBlocked(true);
            giftCard.setBlockedBy(currentUser);
            giftCard.setBlockedDate(java.time.LocalDateTime.now());
            logger.info("Gift card {} blocked by user {}", serialNo, currentUser.getUsername());
        } else {
            if (!giftCard.isBlocked()) {
                logger.warn("Gift card {} is not blocked", serialNo);
                throw new ValidationException(ErrorCodes.GIFT_CARD_INVALID, "Gift card is not blocked");
            }
            giftCard.setBlocked(false);
            giftCard.setBlockedBy(null);
            giftCard.setBlockedDate(null);
            logger.info("Gift card {} unblocked by user {}", serialNo, currentUser.getUsername());
        }

        GiftCard savedGiftCard = giftCardRepository.save(giftCard);
        return giftCardMapper.getFrom(savedGiftCard);
    }

    /**
     * Deactivate or activate a gift card by serial number
     * @param serialNo the serial number of the gift card
     * @param activate true to activate, false to deactivate
     * @return the updated gift card DTO
     * @throws GiftCardNotFoundException if gift card not found
     */
    @Transactional
    public GiftCardDto activateGiftCard(String serialNo, boolean activate) {
        logger.info("{} gift card with serialNo: {}", activate ? "Activating" : "Deactivating", serialNo);
        
        User currentUser = securityContextService.getCurrentUserOrThrow();
        
        GiftCard giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }

        // Check if gift card is blocked - cannot activate/deactivate if blocked
        if (giftCard.isBlocked()) {
            logger.warn("Cannot {} blocked gift card: {}", activate ? "activate" : "deactivate", serialNo);
            throw new ValidationException(ErrorCodes.GIFT_CARD_INVALID, 
                "Cannot " + (activate ? "activate" : "deactivate") + " a blocked gift card. Please unblock it first.");
        }

        // RBAC check: COMPANY_USER and STORE_USER can only activate/deactivate gift cards from their company
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("SUPERADMIN") && !roleName.equals("ADMIN") && !roleName.equals("API_USER")) {
            GiftCardDto dto = giftCardMapper.getFrom(giftCard);
            if (!hasAccessToGiftCard(currentUser, dto)) {
                logger.warn("User {} with role {} does not have access to activate/deactivate gift card {}", 
                           currentUser.getUsername(), roleName, serialNo);
                throw new ValidationException(ErrorCodes.FORBIDDEN, "You do not have access to activate/deactivate this gift card");
            }
        }

        if (activate) {
            if (giftCard.isActive()) {
                logger.warn("Gift card {} is already active", serialNo);
                throw new ValidationException(ErrorCodes.GIFT_CARD_INVALID, "Gift card is already active");
            }
            giftCard.setActive(true);
            logger.info("Gift card {} activated by user {}", serialNo, currentUser.getUsername());
        } else {
            if (!giftCard.isActive()) {
                logger.warn("Gift card {} is already inactive", serialNo);
                throw new ValidationException(ErrorCodes.GIFT_CARD_INVALID, "Gift card is already inactive");
            }
            giftCard.setActive(false);
            logger.info("Gift card {} deactivated by user {}", serialNo, currentUser.getUsername());
        }

        GiftCard savedGiftCard = giftCardRepository.save(giftCard);
        return giftCardMapper.getFrom(savedGiftCard);
    }

    /**
     * Get gift cards for a customer by phone number with RBAC checks
     * @param user the current user making the request
     * @param phoneNumber the phone number of the customer
     * @return list of gift cards for the customer
     * @throws ValidationException if customer not found or user doesn't have access
     */
    @Transactional(readOnly = true)
    public List<GiftCardDto> getGiftCardsByPhoneNumber(User user, String phoneNumber) {
        logger.info("Fetching gift cards for phone number: {} by user: {} with role: {}", 
                   phoneNumber, user.getUsername(), user.getRole().getName());
        
        // Validate phone number
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.error("Phone number is required");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Phone number is required");
        }
        
        // Find customer by phone number
        Customer customer = customerRepository.findByPrimaryPhoneNumber(phoneNumber.trim());
        if (customer == null) {
            logger.warn("Customer not found for phone number: {}", phoneNumber);
            throw new ValidationException(ErrorCodes.CUSTOMER_NOT_FOUND, "Customer not found for phone number: " + phoneNumber);
        }
        
        // Get all gift cards for the customer first
        List<GiftCard> giftCards = giftCardRepository.findByCustomer(customer);
        
        // RBAC check: determine if user has access and filter gift cards based on role
        String roleName = user.getRole().getName();
        List<GiftCard> accessibleGiftCards = new java.util.ArrayList<>();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // These roles have full access to all gift cards
                accessibleGiftCards = giftCards;
                logger.debug("User {} has full access (role: {})", user.getUsername(), roleName);
                break;
                
            case "COMPANY_USER":
                // Access based on company matching: user's company must match gift card's company
                // OR user is requesting their own gift cards
                if (phoneNumber.equals(user.getMobilePhoneNumber())) {
                    // User can always view their own gift cards
                    accessibleGiftCards = giftCards;
                    logger.debug("COMPANY_USER {} viewing own gift cards", user.getUsername());
                } else if (user.getCompany() != null) {
                    // Filter gift cards where company matches user's company
                    accessibleGiftCards = giftCards.stream()
                        .filter(gc -> gc.getCompany() != null && 
                                     gc.getCompany().getId().equals(user.getCompany().getId()))
                        .collect(java.util.stream.Collectors.toList());
                    logger.debug("COMPANY_USER {} access check: {} gift cards accessible from company {}", 
                               user.getUsername(), accessibleGiftCards.size(), user.getCompany().getId());
                } else {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                }
                break;
                
            case "STORE_USER":
                // Access based on store's company matching: gift card's company must match user's store's company
                // OR user is requesting their own gift cards
                if (phoneNumber.equals(user.getMobilePhoneNumber())) {
                    // User can always view their own gift cards
                    accessibleGiftCards = giftCards;
                    logger.debug("STORE_USER {} viewing own gift cards", user.getUsername());
                } else if (user.getStore() != null && user.getStore().getCompany() != null) {
                    // Filter gift cards where company matches user's store's company
                    Long userCompanyId = user.getStore().getCompany().getId();
                    accessibleGiftCards = giftCards.stream()
                        .filter(gc -> gc.getCompany() != null && 
                                     gc.getCompany().getId().equals(userCompanyId))
                        .collect(java.util.stream.Collectors.toList());
                    logger.debug("STORE_USER {} access check: {} gift cards accessible from store's company {}", 
                               user.getUsername(), accessibleGiftCards.size(), userCompanyId);
                } else {
                    logger.warn("STORE_USER {} has no store or company assigned", user.getUsername());
                }
                break;
                
            default:
                // For other roles, only allow if user is requesting their own gift cards
                if (phoneNumber.equals(user.getMobilePhoneNumber())) {
                    accessibleGiftCards = giftCards;
                    logger.debug("User {} with role {} viewing own gift cards", user.getUsername(), roleName);
                } else {
                    logger.debug("User {} with role {} does not have access", user.getUsername(), roleName);
                }
                break;
        }
        
        // Check if user has any access
        if (accessibleGiftCards.isEmpty() && !phoneNumber.equals(user.getMobilePhoneNumber())) {
            logger.warn("User {} with role {} does not have access to gift cards for phone number: {}", 
                       user.getUsername(), roleName, phoneNumber);
            throw new ValidationException(ErrorCodes.FORBIDDEN, "You do not have access to view gift cards for this phone number");
        }
        
        giftCards = accessibleGiftCards;
        
        logger.info("Found {} gift cards for customer with phone number: {}", giftCards.size(), phoneNumber);
        
        return giftCardMapper.getFrom(giftCards);
    }
}


