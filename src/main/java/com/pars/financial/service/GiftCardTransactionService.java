package com.pars.financial.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.TransactionAggregationDto;
import com.pars.financial.dto.TransactionAggregationResponseDto;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.User;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.exception.ClientTransactionIdNotUniqueException;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.GenericException;
import com.pars.financial.exception.GiftCardNotFoundException;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.GiftCardTransactionMapper;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.repository.GiftCardRepository;
import com.pars.financial.repository.GiftCardTransactionRepository;
import com.pars.financial.repository.StoreRepository;

@Service
public class GiftCardTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(GiftCardTransactionService.class);

    @Value("${spring.application.customer.autodefine}")
    private boolean autoDefineCustomer;

    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository transactionRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final CompanyRepository companyRepository;

    private final CustomerService customerService;

    final GiftCardTransactionMapper giftCardTransactionMapper;

    public GiftCardTransactionService(GiftCardRepository giftCardRepository, GiftCardTransactionRepository transactionRepository, StoreRepository storeRpository, CustomerRepository customerRepository, GiftCardTransactionMapper giftCardTransactionMapper, GiftCardTransactionRepository giftCardTransactionRepository, CustomerService customerService, CompanyRepository companyRepository) {
        this.giftCardRepository = giftCardRepository;
        this.transactionRepository = transactionRepository;
        this.storeRepository = storeRpository;
        this.customerRepository = customerRepository;
        this.giftCardTransactionMapper = giftCardTransactionMapper;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.customerService = customerService;
        this.companyRepository = companyRepository;
    }

    private void validateStoreLimit(GiftCard giftCard, Long storeId) {
        logger.debug("Validating store limit for gift card: {} and store: {}", giftCard.getSerialNo(), storeId);
        if (giftCard.isStoreLimited()) {
            boolean storeAllowed = giftCard.getAllowedStores().stream()
                .anyMatch(store -> store.getId().equals(storeId));
            if (!storeAllowed) {
                logger.warn("Store {} not allowed for gift card {}", storeId, giftCard.getSerialNo());
                throw new ValidationException(ErrorCodes.STORE_NOT_ALLOWED, "Gift card cannot be used in this store");
            }
        }
    }

    private void validateCompanyAccess(GiftCard giftCard, Long storeId) {
        logger.debug("Validating company access for gift card: {} and store: {}", giftCard.getSerialNo(), storeId);
        if (giftCard.getCompany() != null) {
            // var store = storeRepository.findById(storeId);
            // if (store.isPresent()) {
            //     Company storeCompany = store.get().getCompany();
            //     if (storeCompany != null && !storeCompany.getId().equals(giftCard.getCompany().getId())) {
            //         logger.warn("Store {} belongs to company {} but gift card {} belongs to company {}", 
            //             storeId, storeCompany.getCompany_name(), giftCard.getSerialNo(), giftCard.getCompany().getCompany_name());
            //         throw new ValidationException("Gift card cannot be used in this store - company mismatch", null, -133);
            //     }
            // }
            logger.debug("Gift card {} belongs to company: {}", giftCard.getSerialNo(), giftCard.getCompany().getName());
        }
    }

    /**
     * Validates customer assignment for gift card transactions
     * Ensures that if a gift card already has a customer, the same customer must be used
     * If no customer is assigned, the provided customer will be assigned
     * Also checks if this is the first transaction based on lastUsed date
     */
    private void validateGiftCardCustomer(GiftCard gc, com.pars.financial.entity.Customer customer) {
        // Check if customer is provided
        if (customer == null) {
            logger.warn("Customer is required for gift card transaction but was not provided");
            throw new ValidationException(ErrorCodes.GIFT_CARD_CUSTOMER_REQUIRED, "Customer is required for gift card transaction");
        }

        // Check if this is the first transaction (lastUsed is null)
        boolean isFirstTransaction = (gc.getLastUsed() == null);
        
        if (isFirstTransaction) {
            // First transaction - customer will be set as the owner
            logger.info("First transaction for gift card {} - setting customer {} as owner", gc.getSerialNo(), customer.getId());
        } else {
            // Not first transaction - must validate customer matches owner
            if (gc.getCustomer() == null) {
                logger.error("Gift card {} has been used before (lastUsed: {}) but has no customer assigned - data integrity issue", 
                    gc.getSerialNo(), gc.getLastUsed());
                throw new ValidationException(ErrorCodes.GIFT_CARD_DATA_INTEGRITY_ERROR, 
                    "Gift card has been used before but has no customer assigned. Data integrity issue.");
            }
            
            // Validate that the current customer is the same as the gift card owner
            if (!Objects.equals(gc.getCustomer().getId(), customer.getId())) {
                logger.warn("Gift card {} is owned by customer {}, but transaction is for customer {}", 
                    gc.getSerialNo(), gc.getCustomer().getId(), customer.getId());
                throw new ValidationException(ErrorCodes.GIFT_CARD_CUSTOMER_MISMATCH, 
                    "Gift card is owned by a different customer. Owner customer ID: " + 
                    gc.getCustomer().getId() + ", but provided customer ID: " + customer.getId());
            }
            logger.debug("Gift card {} customer validation passed - same owner customer {}", gc.getSerialNo(), customer.getId());
        }
    }

    public GiftCardTransactionDto debitGiftCard(User user, String clientTransactionId, long amount, String serialNo, Long storeId, String phoneNumber, long orderAmount) {
        logger.info("Processing debit request for gift card: {}, amount: {}, store: {}, phone: {}, orderAmount: {}", serialNo, amount, storeId, phoneNumber, orderAmount);
        
        if((phoneNumber == null) || (phoneNumber.length() != 11)) {
            logger.warn("Invalid phone number: {}", phoneNumber);
            throw new ValidationException(ErrorCodes.INVALID_PHONE_NUMBER, "Customer Phone Number is incorrect");
        }
        
        var customer = customerRepository.findByPrimaryPhoneNumber(phoneNumber);
        if(customer == null) {
            if (!autoDefineCustomer) {
                logger.warn("Customer not found for phone number: {} and auto-define is disabled", phoneNumber);
                throw new CustomerNotFoundException("Customer not found");
            }
            else {
                logger.info("Auto-defining customer for phone number: {}", phoneNumber);
                customer = customerService.createCustomer(phoneNumber);
            }
        }

        var transactionHistory = giftCardTransactionRepository.findByTransactionTypeAndClientTransactionId(TransactionType.Debit, clientTransactionId);
        if (transactionHistory != null) {
            logger.warn("Duplicate client transaction ID: {}", clientTransactionId);
            throw new ClientTransactionIdNotUniqueException("Client Transaction Id Not Unique");
        }

        var store = storeRepository.findById(storeId);
        if (store.isEmpty()) {
            logger.warn("Store not found with ID: {}", storeId);
            throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
        }

        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc != null){
            validateStoreLimit(gc, storeId);
            validateCompanyAccess(gc, storeId);
            
            // CUSTOMER VALIDATION LOGIC
            validateGiftCardCustomer(gc, customer);
            
            if(gc.getBalance() >= amount) {
                logger.debug("Processing debit transaction for gift card: {}, current balance: {}, debit amount: {}", 
                    gc.getSerialNo(), gc.getBalance(), amount);
                
                // Set customer to gift card only on first transaction (when lastUsed is null)
                if (gc.getLastUsed() == null) {
                    gc.setCustomer(customer);
                    logger.info("First transaction - setting customer {} as owner of gift card {}", customer.getId(), gc.getSerialNo());
                }
                
                GiftCardTransaction transaction = new GiftCardTransaction();
                transaction.setTransactionType(TransactionType.Debit);
                transaction.setGiftCard(gc);
                transaction.setCustomer(customer);
                transaction.setApiUser(user);
                transaction.setStore(store.get());
                transaction.setAmount(amount);
                transaction.setBalanceBefore(gc.getBalance());
                transaction.setOrderAmount(orderAmount);
                transaction.setClientTransactionId(clientTransactionId);
                transaction.setTrxDate(LocalDateTime.now());
                transaction.setStatus(TransactionStatus.Pending);

                gc.setBalance(gc.getBalance() - amount);
                gc.setLastUsed(LocalDateTime.now());
                
                giftCardRepository.save(gc);
                var savedTransaction = transactionRepository.save(transaction);
                
                logger.info("Successfully processed debit transaction: {}, new balance: {}", 
                    savedTransaction.getTransactionId(), gc.getBalance());
                    
                return giftCardTransactionMapper.getFrom(savedTransaction);
            }
            logger.warn("Insufficient balance for gift card: {}, requested: {}, available: {}", 
                gc.getSerialNo(), amount, gc.getBalance());
            throw new GenericException("Not enough balance", null, -120);
        }
        logger.error("Gift card not found: {}", serialNo);
        throw new GenericException("General Failure", null, -121);
    }

    public GiftCardTransactionDto settleTransaction(User user, String clientTransactionId, long amount, String serialNo, UUID transactionId, TransactionType trxType, long orderAmount) {
        logger.info("Processing {} transaction for gift card: {}, amount: {}, transactionId: {}, orderAmount: {}", 
            trxType, serialNo, amount, transactionId, orderAmount);
            
        if(amount <= 0){
            logger.warn("Invalid amount: {}", amount);
            throw new ValidationException(ErrorCodes.INVALID_AMOUNT, "Amount must be greater than 0.");
        }
        
        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc == null) {
            logger.warn("Gift card not found: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found.");
        }
        
        GiftCardTransaction debitTransaction;
        if (trxType == TransactionType.Reversal) {
            logger.debug("Looking up debit transaction by clientTransactionId: {}", clientTransactionId);
            debitTransaction = transactionRepository.findByTransactionTypeAndClientTransactionId(TransactionType.Debit, clientTransactionId);
        } else {
            logger.debug("Looking up debit transaction by transactionId: {}", transactionId);
            debitTransaction = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Debit, transactionId);
        }
        
        if (debitTransaction == null) {
            logger.warn("Debit transaction not found for {}: {}", 
                (trxType == TransactionType.Reversal ? "clientTransactionId" : "transactionId"),
                (trxType == TransactionType.Reversal ? clientTransactionId : transactionId));
            throw new ValidationException(ErrorCodes.TRANSACTION_NOT_FOUND, "Debit Transaction Not Found.");
        } else if (amount != debitTransaction.getAmount()) {    
            logger.warn("Amount mismatch. Expected: {}, Actual: {}", debitTransaction.getAmount(), amount);
            throw new ValidationException(ErrorCodes.TRANSACTION_INVALID, "Debit Amount Not Matched.");
        } else if (!Objects.equals(debitTransaction.getGiftCard().getId(), gc.getId())) {
            logger.warn("Gift card mismatch. Expected: {}, Actual: {}", 
                debitTransaction.getGiftCard().getSerialNo(), gc.getSerialNo());
            throw new ValidationException(ErrorCodes.TRANSACTION_INVALID, "Debit Gift Card Not Matched.");
        }

        var confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
        var reversal = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());

        // Validate transaction state
        switch (trxType) {
            case Credit, Debit, Redeem -> {
                logger.warn("Invalid transaction type: {}", trxType);
                throw new ValidationException(ErrorCodes.TRANSACTION_INVALID);
            }
            case Reversal, Confirmation -> {
                if (confirmation != null) {
                    logger.warn("Transaction already confirmed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException(ErrorCodes.TRANSACTION_ALREADY_CONFIRMED);
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException(ErrorCodes.TRANSACTION_ALREADY_REVERSED);
                }
            }
            case Refund -> {
                var refund = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, debitTransaction.getTransactionId());
                if (refund != null) {
                    logger.warn("Transaction already refunded: {}", debitTransaction.getTransactionId());
                    throw new ValidationException(ErrorCodes.TRANSACTION_ALREADY_REFUNDED);
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException(ErrorCodes.TRANSACTION_ALREADY_REVERSED);
                }
                if(confirmation == null) {
                    logger.warn("Transaction not confirmed yet: {}", debitTransaction.getTransactionId());
                    throw new ValidationException(ErrorCodes.TRANSACTION_NOT_CONFIRMED);
                }
            }
        }

        if(((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) && (gc.getInitialAmount() < gc.getBalance() + amount)) {
            logger.warn("Amount error. Initial: {}, Current: {}, Add: {}", 
                gc.getInitialAmount(), gc.getBalance(), amount);
            throw new ValidationException(ErrorCodes.INVALID_AMOUNT, "Amount Error."); 
        }

        logger.debug("Creating {} transaction for debit transaction: {}", trxType, debitTransaction.getTransactionId());
        
        // Validate that the debit transaction has a customer
        if (debitTransaction.getCustomer() == null) {
            logger.warn("Debit transaction {} has no customer assigned", debitTransaction.getTransactionId());
            throw new ValidationException(ErrorCodes.GIFT_CARD_CUSTOMER_REQUIRED, "Debit transaction must have a customer assigned");
        }
        
        GiftCardTransaction transaction = new GiftCardTransaction();
        transaction.setCustomer(debitTransaction.getCustomer());
        transaction.setApiUser(user);
        transaction.setDebitTransaction(debitTransaction);
        transaction.setClientTransactionId(debitTransaction.getClientTransactionId());
        transaction.setTransactionId(debitTransaction.getTransactionId());
        transaction.setTransactionType(trxType);
        transaction.setGiftCard(gc);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(gc.getBalance());
        transaction.setOrderAmount(orderAmount);
        transaction.setTrxDate(LocalDateTime.now());
        transaction.setStore(debitTransaction.getStore());
        
        if ((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) {
            gc.setBalance(gc.getBalance() + amount);
        }
        gc.setLastUsed(LocalDateTime.now());
        
        giftCardRepository.save(gc);
        
        switch (trxType) {
            case Reversal -> {
                transaction.setStatus(TransactionStatus.Reversed);
                debitTransaction.setStatus(TransactionStatus.Reversed);
            }
            case Refund -> {
                transaction.setStatus(TransactionStatus.Refunded);
                confirmation.setStatus(TransactionStatus.Refunded);
                debitTransaction.setStatus(TransactionStatus.Refunded);
            }
            case Confirmation -> {
                transaction.setStatus(TransactionStatus.Confirmed);
                debitTransaction.setStatus(TransactionStatus.Confirmed);
            }
        }
        var savedTransaction = transactionRepository.save(transaction);
        transactionRepository.save(debitTransaction);
        
        if(trxType == TransactionType.Refund) {
            transactionRepository.save(confirmation);
        }

        logger.info("Successfully processed {} transaction: {}, new balance: {}", 
            trxType, savedTransaction.getTransactionId(), gc.getBalance());
            
        return giftCardTransactionMapper.getFrom(savedTransaction);
    }

    public GiftCardTransactionDto reverseTransaction(User user, String clientTransactionId, long amount, String serialNo, UUID transactionId, long orderAmount) {
        logger.info("Initiating reverse transaction for gift card: {}, amount: {}, orderAmount: {}", serialNo, amount, orderAmount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Reversal, orderAmount);
    }

    public GiftCardTransactionDto confirmTransaction(User user, String clientTransactionId, long amount, String serialNo, UUID transactionId, long orderAmount) {
        logger.info("Initiating confirm transaction for gift card: {}, amount: {}, orderAmount: {}", serialNo, amount, orderAmount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Confirmation, orderAmount);
    }

    public GiftCardTransactionDto refundTransaction(User user, String clientTransactionId, long amount, String serialNo, UUID transactionId, long orderAmount) {
        logger.info("Initiating refund transaction for gift card: {}, amount: {}, orderAmount: {}", serialNo, amount, orderAmount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Refund, orderAmount);
    }

    public GiftCardTransactionDto get(String trxId) {
        logger.debug("Fetching transaction details for ID: {}", trxId);
        return giftCardTransactionMapper.getFrom(transactionRepository.findByTransactionId(UUID.fromString(trxId)));
    }

    public GiftCardTransactionDto checkStatus(String clientTransactionId) {
        logger.debug("Checking transaction status for clientTransactionId: {}", clientTransactionId);
        return giftCardTransactionMapper.getFrom(transactionRepository.findByClientTransactionId(clientTransactionId));
    }

    public PagedResponse<GiftCardTransactionDto> getTransactionHistory(String serialNo, int page, int size) {
        logger.debug("Fetching transaction history for gift card: {} with pagination - page: {}, size: {}", serialNo, page, size);
        
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if(gc != null) {
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
            Page<GiftCardTransaction> transactionPage = transactionRepository.findByGiftCardAndTransactionType(gc, TransactionType.Debit, pageable);
            
            List<GiftCardTransactionDto> transactions = giftCardTransactionMapper.getFrom(transactionPage.getContent());
            
            return new PagedResponse<>(
                transactions,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
            );
        }
        logger.warn("Gift card not found: {}", serialNo);
        throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
    }

    /**
     * Get all gift cards for a specific company with pagination
     * @param companyId the company ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated list of gift card transaction DTOs
     */
    public PagedResponse<GiftCardTransactionDto> getGiftCardsByCompany(Long companyId, int page, int size) {
        logger.debug("Fetching gift cards for company: {} with pagination - page: {}, size: {}", companyId, page, size);
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
        
        List<GiftCardTransactionDto> transactions = giftCardPage.getContent().stream()
            .map(gc -> {
                // Get the latest transaction for each gift card
                var latestTransaction = transactionRepository.findTopByGiftCardOrderByTrxDateDesc(gc);
                return latestTransaction != null ? giftCardTransactionMapper.getFrom(latestTransaction) : null;
            })
            .filter(Objects::nonNull)
            .toList();
        
        return new PagedResponse<>(
            transactions,
            giftCardPage.getNumber(),
            giftCardPage.getSize(),
            giftCardPage.getTotalElements(),
            giftCardPage.getTotalPages()
        );
    }

    /**
     * Get transaction history for all gift cards of a company with pagination
     * @param companyId the company ID
     * @param page the page number (0-based)
     * @param size the page size
     * @return paginated list of gift card transaction DTOs
     */
    public PagedResponse<GiftCardTransactionDto> getCompanyTransactionHistory(Long companyId, int page, int size) {
        logger.debug("Fetching transaction history for company: {} with pagination - page: {}, size: {}", companyId, page, size);
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
        Page<GiftCardTransaction> transactionPage = transactionRepository.findByGiftCardCompany(company.get(), pageable);
        
        List<GiftCardTransactionDto> transactions = giftCardTransactionMapper.getFrom(transactionPage.getContent());
        
        return new PagedResponse<>(
            transactions,
            transactionPage.getNumber(),
            transactionPage.getSize(),
            transactionPage.getTotalElements(),
            transactionPage.getTotalPages()
        );
    }

    /**
     * Get transaction aggregations for today, last 7 days, and last 30 days
     * For STORE_USER, only returns data for their assigned store
     * For SUPERADMIN, returns data for all stores
     */
    public TransactionAggregationResponseDto getTransactionAggregations(User user) {
        logger.info("Getting transaction aggregations for user: {}", user.getUsername());
        
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        
        LocalDateTime last7DaysStart = today.minusDays(7).atStartOfDay();
        LocalDateTime last30DaysStart = today.minusDays(30).atStartOfDay();
        
        List<TransactionAggregationDto> todayAggregations;
        List<TransactionAggregationDto> last7DaysAggregations;
        List<TransactionAggregationDto> last30DaysAggregations;
        
        // Check if user is STORE_USER and filter by store
        if (user.getRole().getName().equals("STORE_USER") && user.getStore() != null) {
            Long storeId = user.getStore().getId();
            logger.debug("Filtering aggregations for store: {}", storeId);
            
            todayAggregations = getAggregationsForDateRangeAndStore(todayStart, todayEnd, storeId);
            last7DaysAggregations = getAggregationsForDateRangeAndStore(last7DaysStart, todayEnd, storeId);
            last30DaysAggregations = getAggregationsForDateRangeAndStore(last30DaysStart, todayEnd, storeId);
        } else {
            // SUPERADMIN or other roles - get all transactions
            logger.debug("Getting aggregations for all stores");
            todayAggregations = getAggregationsForDateRange(todayStart, todayEnd);
            last7DaysAggregations = getAggregationsForDateRange(last7DaysStart, todayEnd);
            last30DaysAggregations = getAggregationsForDateRange(last30DaysStart, todayEnd);
        }
        
        return new TransactionAggregationResponseDto(todayAggregations, last7DaysAggregations, last30DaysAggregations);
    }
    
    private List<TransactionAggregationDto> getAggregationsForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = transactionRepository.getTransactionAggregationsByDateRange(startDate, endDate);
        return convertToAggregationDtos(results);
    }
    
    private List<TransactionAggregationDto> getAggregationsForDateRangeAndStore(LocalDateTime startDate, LocalDateTime endDate, Long storeId) {
        List<Object[]> results = transactionRepository.getTransactionAggregationsByDateRangeAndStore(startDate, endDate, storeId);
        return convertToAggregationDtos(results);
    }
    
    /**
     * Creates a default empty aggregation list with all three statuses set to zero
     */
    private List<TransactionAggregationDto> createEmptyAggregations() {
        List<TransactionAggregationDto> emptyAggregations = new ArrayList<>();
        TransactionStatus[] statuses = {TransactionStatus.Confirmed, TransactionStatus.Refunded, TransactionStatus.Reversed};
        
        for (TransactionStatus status : statuses) {
            emptyAggregations.add(new TransactionAggregationDto(status, 0L, 0L, 0L));
        }
        
        logger.debug("Created empty aggregations with {} statuses", emptyAggregations.size());
        return emptyAggregations;
    }
    
    private List<TransactionAggregationDto> convertToAggregationDtos(List<Object[]> results) {
        List<TransactionAggregationDto> aggregations = new ArrayList<>();
        
        // Handle null or empty results
        if (results == null || results.isEmpty()) {
            logger.debug("No results from database query, returning empty aggregations");
            return createEmptyAggregations();
        }
        
        logger.debug("Converting {} result rows to aggregation DTOs", results.size());
        
        for (Object[] result : results) {
            TransactionStatus status = (TransactionStatus) result[0];
            Long count = ((Number) result[1]).longValue();
            Long totalAmount = ((Number) result[2]).longValue();
            Long totalOrderAmount = ((Number) result[3]).longValue();
            
            aggregations.add(new TransactionAggregationDto(status, count, totalAmount, totalOrderAmount));
            logger.debug("Added aggregation for status {}: count={}, totalAmount={}, totalOrderAmount={}", 
                        status, count, totalAmount, totalOrderAmount);
        }
        
        // Ensure we have entries for all three statuses (Confirmed, Refunded, Reversed)
        // If no transactions exist for a status, add with 0 count and amount
        ensureAllStatusesPresent(aggregations);
        
        logger.debug("Final aggregation list size: {}", aggregations.size());
        return aggregations;
    }
    
    private void ensureAllStatusesPresent(List<TransactionAggregationDto> aggregations) {
        TransactionStatus[] requiredStatuses = {TransactionStatus.Confirmed, TransactionStatus.Refunded, TransactionStatus.Reversed};
        
        logger.debug("Ensuring all statuses are present. Current size: {}", aggregations.size());
        
        for (TransactionStatus status : requiredStatuses) {
            boolean exists = aggregations.stream()
                .anyMatch(agg -> agg.status == status);
            
            if (!exists) {
                logger.debug("Adding missing status {} with zero values", status);
                aggregations.add(new TransactionAggregationDto(status, 0L, 0L, 0L));
            } else {
                logger.debug("Status {} already exists in aggregations", status);
            }
        }
        
        logger.debug("Final status count: {}", aggregations.size());
    }
}
