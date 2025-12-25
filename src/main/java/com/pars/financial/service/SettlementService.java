package com.pars.financial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.CompanySettlementDto;
import com.pars.financial.dto.SettlementReportDto;
import com.pars.financial.dto.SettlementTransactionDetailDto;
import com.pars.financial.dto.StoreSettlementDto;
import com.pars.financial.entity.Company;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.GiftCardTransactionRepository;
import com.pars.financial.repository.StoreRepository;

@Service
public class SettlementService {

    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final StoreRepository storeRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextService securityContextService;

    public SettlementService(
            GiftCardTransactionRepository giftCardTransactionRepository,
            StoreRepository storeRepository,
            CompanyRepository companyRepository,
            SecurityContextService securityContextService) {
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.storeRepository = storeRepository;
        this.companyRepository = companyRepository;
        this.securityContextService = securityContextService;
    }

    /**
     * Generate settlement report for fulfilled gift card transactions
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @param storeId Optional store filter
     * @param companyId Optional company filter (for COMPANY_USER, must match their company or will be auto-set)
     * @return Settlement report
     */
    @Transactional(readOnly = true)
    public SettlementReportDto generateSettlementReport(LocalDate startDate, LocalDate endDate, Long storeId, Long companyId) {
        User currentUser = securityContextService.getCurrentUserOrThrow();
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        
        // Handle companyId access control for COMPANY_USER
        Long effectiveCompanyId = resolveCompanyId(currentUser, companyId);
        if (effectiveCompanyId == null && companyId != null) {
            // COMPANY_USER tried to access a different company - access denied
            logger.warn("COMPANY_USER {} attempted to access settlement report for company {} but belongs to company {}", 
                       currentUser.getUsername(), companyId, 
                       currentUser.getCompany() != null ? currentUser.getCompany().getId() : "none");
            throw new IllegalArgumentException("Access denied: You can only access settlement reports for your own company");
        }
        
        logger.info("Generating settlement report for user {} (role: {}), date range: {} to {}, storeId: {}, companyId: {}", 
                   currentUser.getUsername(), roleName, startDate, endDate, storeId, effectiveCompanyId);

        // Convert dates to LocalDateTime
        // Start date: beginning of the day (inclusive)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        // End date: end of the day (inclusive) - add 1 day and use < in query
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Fetch transactions based on filters and user role
        List<GiftCardTransaction> transactions = fetchTransactions(startDateTime, endDateTime, storeId, effectiveCompanyId, currentUser);

        if (transactions.isEmpty()) {
            logger.info("No fulfilled transactions found for the given criteria");
            return createEmptyReport(startDateTime, endDateTime, storeId, effectiveCompanyId);
        }

        // Build settlement report
        SettlementReportDto report = new SettlementReportDto();
        report.setReportDate(LocalDate.now());
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDateTime(startDateTime);
        report.setEndDateTime(endDateTime);
        
        if (storeId != null) {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                report.setFilterStoreId(storeId);
                report.setFilterStoreName(store.getStore_name());
            }
        }
        
        if (effectiveCompanyId != null) {
            Company company = companyRepository.findById(effectiveCompanyId).orElse(null);
            if (company != null) {
                report.setFilterCompanyId(effectiveCompanyId);
                report.setFilterCompanyName(company.getName());
            }
        }

        // Process transactions and build settlement data
        // Map: companyId -> CompanySettlementDto
        Map<Long, CompanySettlementDto> companyMap = new HashMap<>();
        // Map: companyId -> Map<storeId -> StoreSettlementDto>
        Map<Long, Map<Long, StoreSettlementDto>> companyStoreMap = new HashMap<>();
        List<SettlementTransactionDetailDto> transactionDetails = new ArrayList<>();

        BigDecimal totalPayable = BigDecimal.ZERO;
        BigDecimal totalReceivable = BigDecimal.ZERO;

        for (GiftCardTransaction transaction : transactions) {
            if (transaction.getGiftCard() == null || transaction.getStore() == null) {
                logger.warn("Transaction {} has null giftCard or store, skipping", transaction.getId());
                continue;
            }

            Company issuerCompany = transaction.getGiftCard().getCompany();
            Store usageStore = transaction.getStore();
            Company usageStoreCompany = usageStore.getCompany();

            if (issuerCompany == null || usageStoreCompany == null) {
                logger.warn("Transaction {} has null issuer company or usage store company, skipping", transaction.getId());
                continue;
            }

            // Skip if issuer and usage store belong to the same company (no settlement needed)
            if (issuerCompany.getId().equals(usageStoreCompany.getId())) {
                continue;
            }

            BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

            // Create transaction detail
            SettlementTransactionDetailDto detail = createTransactionDetail(transaction, issuerCompany, usageStore, usageStoreCompany);
            transactionDetails.add(detail);

            // Issuer company owes money (PAYABLE)
            CompanySettlementDto issuerCompanyDto = companyMap.computeIfAbsent(
                issuerCompany.getId(),
                id -> createCompanySettlement(issuerCompany)
            );
            issuerCompanyDto.setTotalPayable(issuerCompanyDto.getTotalPayable().add(amount));
            totalPayable = totalPayable.add(amount);
            
            // Usage store company receives money (RECEIVABLE)
            CompanySettlementDto usageCompanyDto = companyMap.computeIfAbsent(
                usageStoreCompany.getId(),
                id -> createCompanySettlement(usageStoreCompany)
            );
            usageCompanyDto.setTotalReceivable(usageCompanyDto.getTotalReceivable().add(amount));
            totalReceivable = totalReceivable.add(amount);
            
            // Update store settlements under usage store company (RECEIVABLE)
            Map<Long, StoreSettlementDto> storeMap = companyStoreMap.computeIfAbsent(
                usageStoreCompany.getId(),
                id -> new HashMap<>()
            );
            StoreSettlementDto storeDto = storeMap.computeIfAbsent(
                usageStore.getId(),
                id -> createStoreSettlement(usageStore)
            );
            storeDto.setReceivable(storeDto.getReceivable().add(amount));
            storeDto.setTransactionCount(storeDto.getTransactionCount() + 1);
        }

        // Calculate net amounts and link stores to their companies
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Map.Entry<Long, CompanySettlementDto> entry : companyMap.entrySet()) {
            Long compId = entry.getKey();
            CompanySettlementDto companyDto = entry.getValue();
            
            // Calculate company net amount
            BigDecimal companyNet = companyDto.getTotalReceivable().subtract(companyDto.getTotalPayable());
            companyDto.setNetAmount(companyNet);
            
            // Get stores for this company
            Map<Long, StoreSettlementDto> storeMap = companyStoreMap.getOrDefault(compId, new HashMap<>());
            
            // Calculate store net amounts
            for (StoreSettlementDto storeDto : storeMap.values()) {
                BigDecimal storeNet = storeDto.getReceivable().subtract(storeDto.getPayable());
                storeDto.setNetAmount(storeNet);
            }
            
            companyDto.setStores(new ArrayList<>(storeMap.values()));
            
            // Add company net amount to grand total
            grandTotal = grandTotal.add(companyNet);
        }

        report.setTotalPayable(totalPayable);
        report.setTotalReceivable(totalReceivable);
        report.setGrandTotal(grandTotal);
        report.setNetSettlement(grandTotal);
        report.setCompanies(new ArrayList<>(companyMap.values()));
        report.setTransactionDetails(transactionDetails);

        logger.info("Settlement report generated: {} companies, {} transactions, Grand Total: {}", 
                   companyMap.size(), transactionDetails.size(), grandTotal);

        return report;
    }

    /**
     * Resolve companyId based on user role and provided companyId
     * For COMPANY_USER: if companyId is provided, validate it matches their company; if not, use their companyId
     * For other roles: return provided companyId as-is
     */
    private Long resolveCompanyId(User currentUser, Long companyId) {
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        
        if ("COMPANY_USER".equals(roleName)) {
            Long userCompanyId = currentUser.getCompany() != null ? currentUser.getCompany().getId() : null;
            if (userCompanyId == null) {
                logger.warn("COMPANY_USER {} has no company assigned", currentUser.getUsername());
                return null;
            }
            
            // If companyId is provided, validate it matches user's company
            if (companyId != null) {
                if (!userCompanyId.equals(companyId)) {
                    // Access denied - return null to signal access violation
                    return null;
                }
                return companyId;
            }
            
            // If companyId is not provided, use user's companyId
            return userCompanyId;
        }
        
        // For other roles, return provided companyId as-is
        return companyId;
    }

    /**
     * Fetch transactions based on filters and user role
     */
    private List<GiftCardTransaction> fetchTransactions(LocalDateTime startDateTime, LocalDateTime endDateTime, 
                                                         Long storeId, Long companyId, User currentUser) {
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : "";

        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                if (storeId != null && companyId != null) {
                    // Filter by both store and company
                    List<GiftCardTransaction> transactions = giftCardTransactionRepository.findFulfilledTransactionsForSettlementByStore(
                        startDateTime, endDateTime, storeId);
                    // Filter by company (issuer or usage store company)
                    return transactions.stream()
                        .filter(t -> {
                            Company issuerCompany = t.getGiftCard() != null ? t.getGiftCard().getCompany() : null;
                            Company usageCompany = t.getStore() != null ? t.getStore().getCompany() : null;
                            return (issuerCompany != null && issuerCompany.getId().equals(companyId)) ||
                                   (usageCompany != null && usageCompany.getId().equals(companyId));
                        })
                        .collect(Collectors.toList());
                } else if (storeId != null) {
                    return giftCardTransactionRepository.findFulfilledTransactionsForSettlementByStore(
                        startDateTime, endDateTime, storeId);
                } else if (companyId != null) {
                    // Filter by company (get transactions where company is issuer or usage store company)
                    List<GiftCardTransaction> issuerTransactions = giftCardTransactionRepository
                        .findFulfilledTransactionsForSettlementByIssuerCompany(startDateTime, endDateTime, companyId);
                    List<GiftCardTransaction> usageTransactions = giftCardTransactionRepository
                        .findFulfilledTransactionsForSettlementByStoreCompany(startDateTime, endDateTime, companyId);
                    
                    // Combine and deduplicate
                    Map<Long, GiftCardTransaction> uniqueTransactions = new HashMap<>();
                    issuerTransactions.forEach(t -> uniqueTransactions.put(t.getId(), t));
                    usageTransactions.forEach(t -> uniqueTransactions.put(t.getId(), t));
                    return new ArrayList<>(uniqueTransactions.values());
                } else {
                    return giftCardTransactionRepository.findFulfilledTransactionsForSettlement(
                        startDateTime, endDateTime);
                }

            case "COMPANY_USER":
                // companyId is already resolved to user's companyId in resolveCompanyId
                if (companyId == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", currentUser.getUsername());
                    return new ArrayList<>();
                }
                
                if (storeId != null) {
                    // Filter by store and ensure it belongs to user's company or check if gift cards from user's company were used
                    List<GiftCardTransaction> transactions = giftCardTransactionRepository.findFulfilledTransactionsForSettlementByStore(
                        startDateTime, endDateTime, storeId);
                    // Filter to include only transactions where user's company is issuer or usage store company
                    return transactions.stream()
                        .filter(t -> {
                            Company issuerCompany = t.getGiftCard() != null ? t.getGiftCard().getCompany() : null;
                            Company usageCompany = t.getStore() != null ? t.getStore().getCompany() : null;
                            return (issuerCompany != null && issuerCompany.getId().equals(companyId)) ||
                                   (usageCompany != null && usageCompany.getId().equals(companyId));
                        })
                        .collect(Collectors.toList());
                } else {
                    // Get transactions where user's company is issuer or usage store company
                    List<GiftCardTransaction> issuerTransactions = giftCardTransactionRepository
                        .findFulfilledTransactionsForSettlementByIssuerCompany(startDateTime, endDateTime, companyId);
                    List<GiftCardTransaction> usageTransactions = giftCardTransactionRepository
                        .findFulfilledTransactionsForSettlementByStoreCompany(startDateTime, endDateTime, companyId);
                    
                    // Combine and deduplicate
                    Map<Long, GiftCardTransaction> uniqueTransactions = new HashMap<>();
                    issuerTransactions.forEach(t -> uniqueTransactions.put(t.getId(), t));
                    usageTransactions.forEach(t -> uniqueTransactions.put(t.getId(), t));
                    return new ArrayList<>(uniqueTransactions.values());
                }

            default:
                logger.warn("User {} with role {} is not authorized for settlement report", 
                           currentUser.getUsername(), roleName);
                return new ArrayList<>();
        }
    }

    /**
     * Create an empty settlement report
     */
    private SettlementReportDto createEmptyReport(LocalDateTime startDateTime, LocalDateTime endDateTime, Long storeId, Long companyId) {
        SettlementReportDto report = new SettlementReportDto();
        report.setReportDate(LocalDate.now());
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDateTime(startDateTime);
        report.setEndDateTime(endDateTime);
        report.setCompanies(new ArrayList<>());
        report.setTransactionDetails(new ArrayList<>());
        
        if (storeId != null) {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                report.setFilterStoreId(storeId);
                report.setFilterStoreName(store.getStore_name());
            }
        }
        
        if (companyId != null) {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company != null) {
                report.setFilterCompanyId(companyId);
                report.setFilterCompanyName(company.getName());
            }
        }
        
        return report;
    }

    /**
     * Create company settlement DTO
     */
    private CompanySettlementDto createCompanySettlement(Company company) {
        CompanySettlementDto settlement = new CompanySettlementDto();
        settlement.setCompanyId(company.getId());
        settlement.setCompanyName(company.getName());
        settlement.setStores(new ArrayList<>());
        return settlement;
    }

    /**
     * Create store settlement DTO
     */
    private StoreSettlementDto createStoreSettlement(Store store) {
        StoreSettlementDto settlement = new StoreSettlementDto();
        settlement.setStoreId(store.getId());
        settlement.setStoreName(store.getStore_name());
        return settlement;
    }

    /**
     * Create transaction detail DTO
     */
    private SettlementTransactionDetailDto createTransactionDetail(GiftCardTransaction transaction,
                                                                  Company issuerCompany,
                                                                  Store usageStore,
                                                                  Company usageStoreCompany) {
        SettlementTransactionDetailDto detail = new SettlementTransactionDetailDto();
        detail.setTransactionId(transaction.getId());
        detail.setTransactionUuid(transaction.getTransactionId());
        detail.setTransactionDate(transaction.getTrxDate());
        
        if (transaction.getGiftCard() != null) {
            detail.setGiftCardId(transaction.getGiftCard().getId());
            detail.setGiftCardSerialNo(transaction.getGiftCard().getSerialNo());
        }
        
        detail.setIssuerCompanyId(issuerCompany.getId());
        detail.setIssuerCompanyName(issuerCompany.getName());
        
        detail.setUsageStoreId(usageStore.getId());
        detail.setUsageStoreName(usageStore.getStore_name());
        detail.setUsageStoreCompanyId(usageStoreCompany.getId());
        detail.setUsageStoreCompanyName(usageStoreCompany.getName());
        
        detail.setTransactionAmount(BigDecimal.valueOf(transaction.getAmount()));
        detail.setOrderAmount(BigDecimal.valueOf(transaction.getOrderAmount()));
        
        // Settlement type: issuer company owes (PAYABLE), usage store company receives (RECEIVABLE)
        detail.setSettlementType("PAYABLE"); // From issuer company perspective
        
        if (transaction.getCustomer() != null) {
            detail.setCustomerId(transaction.getCustomer().getId());
            detail.setCustomerName(transaction.getCustomer().getName() + " " + 
                                 (transaction.getCustomer().getSurname() != null ? transaction.getCustomer().getSurname() : ""));
            detail.setCustomerPhoneNumber(transaction.getCustomer().getPrimaryPhoneNumber());
        }
        
        detail.setOrderNumber(transaction.getOrderno());
        detail.setDescription(transaction.getDescription());
        
        return detail;
    }
}

