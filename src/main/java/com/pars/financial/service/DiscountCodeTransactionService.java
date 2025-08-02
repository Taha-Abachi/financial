package com.pars.financial.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.DiscountCodeTransactionMapper;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.repository.DiscountCodeTransactionRepository;
import com.pars.financial.repository.StoreRepository;

@Service
public class DiscountCodeTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountCodeTransactionService.class);

    @Value("${spring.application.customer.autodefine}")
    private boolean autoDefineCustomer;

    private final DiscountCodeRepository codeRepository;
    private final DiscountCodeTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    private final CustomerService customerService;

    private final DiscountCodeTransactionMapper mapper;

    public DiscountCodeTransactionService(DiscountCodeRepository discountCodeRepository, DiscountCodeTransactionRepository discountCodeTransactionRepository, CustomerRepository customerRepository, StoreRepository storeRepository, CustomerService customerService, DiscountCodeTransactionMapper mapper) {
        this.codeRepository = discountCodeRepository;
        this.transactionRepository = discountCodeTransactionRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.customerService = customerService;
        this.mapper = mapper;
    }

    public DiscountCodeTransactionDto getTransaction(UUID transactionId) {
        logger.debug("Fetching discount code transaction: {}", transactionId);
        var transaction = transactionRepository.findByTransactionIdAndTrxType(transactionId, TransactionType.Redeem);
        if (transaction == null) {
            logger.warn("Transaction not found: {}", transactionId);
        }
        assert transaction != null;
        return mapper.getFrom(transaction);
    }

    public DiscountCodeTransactionDto redeem(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Processing redeem request for discount code: {}, amount: {}, store: {}, phone: {}", 
            dto.code, dto.originalAmount, dto.storeId, dto.phoneNo);
            
        var code = codeRepository.findByCode(dto.code);
        if(code == null) {
            logger.warn("Discount code not found: {}", dto.code);
            throw new ValidationException("Discount Code not found.", null, -104);
        }
        else if(LocalDate.now().isAfter(code.getExpiryDate())){
            logger.warn("Discount code is expired: {}", dto.code);
            throw new ValidationException("Discount code is expired.", null, -141);
        }
        else if(!code.isActive()) {
            logger.warn("Discount code is inactive: {}", dto.code);
            throw new ValidationException("Discount code is inactive.", null, -105);
        }
        else if(code.isUsed()) {
            logger.warn("Discount code already used: {}", dto.code);
            throw new ValidationException("Discount already used.", null, -106);
        }
        
        if (code.getCurrentUsageCount() >= code.getUsageLimit()) {
            logger.warn("Discount code usage limit reached: {} (current: {}, limit: {})", 
                dto.code, code.getCurrentUsageCount(), code.getUsageLimit());
            throw new ValidationException("Discount code usage limit reached.", null, -107);
        }
        
        if (code.getMinimumBillAmount() > 0 && dto.originalAmount < code.getMinimumBillAmount()) {
            logger.warn("Original amount {} is less than minimum bill amount {} for discount code: {}", 
                dto.originalAmount, code.getMinimumBillAmount(), dto.code);
            throw new ValidationException("Original amount is less than minimum bill amount required.", null, -108);
        }
        
        var transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        if(transaction != null){
            logger.warn("Duplicate client transaction ID: {}", dto.clientTransactionId);
            throw new ValidationException("ClientTransactionId should be unique.", null, -109);
        }
        
        var store = storeRepository.findById(dto.storeId);
        if (store.isEmpty()) {
            logger.warn("Store not found: {}", dto.storeId);
            throw new ValidationException("Store Not Found", null, -110);
        }
        // Store limitation check
        if (code.isStoreLimited()) {
            boolean storeAllowed = code.getAllowedStores().stream()
                .anyMatch(s -> s.getId().equals(dto.storeId));
            if (!storeAllowed) {
                logger.warn("Store {} not allowed for discount code {}", dto.storeId, code.getCode());
                throw new ValidationException("Discount code cannot be used in this store", null, -117);
            }
        }
        
        var customer = customerRepository.findByPrimaryPhoneNumber(dto.phoneNo);
        if(customer == null){
            if (!autoDefineCustomer) {
                logger.warn("Customer not found for phone number: {} and auto-define is disabled", dto.phoneNo);
                throw new CustomerNotFoundException("Customer not found");
            }
            else {
                logger.info("Auto-defining customer for phone number: {}", dto.phoneNo);
                customer = customerService.createCustomer(dto.phoneNo);
            }
        }

        long discount;
        switch (code.getDiscountType()) {
            case FREEDELIVERY -> {
                discount = 0;
                logger.debug("Using free delivery discount amount: {}", discount);
            }
            case CONSTANT -> {
                discount = code.getConstantDiscountAmount();
                logger.debug("Using constant discount amount: {}", discount);
            }
            default -> {
                // PERCENTAGE
                discount = (long) (code.getPercentage() * dto.originalAmount / 100);
                if(code.getMaxDiscountAmount() > 0) {
                    discount = Math.min(discount, code.getMaxDiscountAmount());
                }
                logger.debug("Calculated percentage-based discount amount: {} ({}% of {})", discount, code.getPercentage(), dto.originalAmount);
            }
        }

        transaction = new DiscountCodeTransaction();
        transaction.setDiscountCode(code);
        transaction.setTrxType(TransactionType.Redeem);
        transaction.setClientTransactionId(dto.clientTransactionId);
        transaction.setDiscountAmount(discount);
        transaction.setOriginalAmount(dto.originalAmount);
        transaction.setApiUser(apiUser);
        transaction.setCustomer(customer);
        transaction.setStore(store.get());
        transaction.setTrxDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.Pending);
        var savedTransaction = transactionRepository.save(transaction);

        code.setRedeemDate(LocalDateTime.now());
        code.setUsed(code.getCurrentUsageCount() + 1 >= code.getUsageLimit());
        code.setCurrentUsageCount(code.getCurrentUsageCount() + 1);
        codeRepository.save(code);

        logger.info("Successfully processed redeem transaction: {}, discount amount: {}", 
            savedTransaction.getTransactionId(), discount);
        return mapper.getFrom(savedTransaction);
    }

    public DiscountCodeTransaction settleTransaction(ApiUser apiUser, DiscountCodeTransactionDto dto, TransactionType trxType) {
        logger.info("Processing {} transaction for discount code transaction: {}", trxType, dto.transactionId);
        
        DiscountCodeTransaction transaction = null;
        if(trxType == TransactionType.Confirmation) {
            logger.debug("Looking up redeem transaction by transactionId: {}", dto.transactionId);
            transaction = transactionRepository.findByTransactionIdAndTrxType(dto.transactionId, TransactionType.Redeem);
        }
        else if(trxType == TransactionType.Reversal) {
            logger.debug("Looking up redeem transaction by clientTransactionId: {}", dto.clientTransactionId);
            transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        }
        else if(trxType == TransactionType.Refund) {
            logger.debug("Looking up redeem transaction by transactionId: {}", dto.transactionId);
            transaction = transactionRepository.findByTransactionIdAndTrxType(dto.transactionId, TransactionType.Redeem);
        }

        if(transaction == null) {
            logger.warn("Redeem transaction not found for {}: {}", 
                (trxType == TransactionType.Confirmation || trxType == TransactionType.Refund ? "transactionId" : "clientTransactionId"),
                (trxType == TransactionType.Confirmation || trxType == TransactionType.Refund ? dto.transactionId : dto.clientTransactionId));
            throw new ValidationException("Redeem Transaction Not Found.", null, -112);
        }

        var confirmation = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Confirmation);
        if((confirmation != null) && (trxType != TransactionType.Refund)) {
            logger.warn("Transaction already confirmed: {}", transaction.getTransactionId());
            throw new ValidationException("Transaction already confirmed.", null, -113);
        }
        
        var reversal = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Reversal);
        if(reversal != null) {
            logger.warn("Transaction already reversed: {}", transaction.getTransactionId());
            throw new ValidationException("Transaction already reversed.", null, -114);
        }

        // Additional validation for refund
        if(trxType == TransactionType.Refund) {
            var refund = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Refund);
            if (refund != null) {
                logger.warn("Transaction already refunded: {}", transaction.getTransactionId());
                throw new ValidationException("Transaction already refunded.", null, -115);
            }

            //confirmation = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Confirmation);
            if(confirmation == null) {
                logger.warn("Transaction not confirmed yet: {}", transaction.getTransactionId());
                throw new ValidationException("Transaction not confirmed yet.", null, -116);
            }
        }

        logger.debug("Creating {} transaction for redeem transaction: {}", trxType, transaction.getTransactionId());
        DiscountCodeTransaction nTransaction = new DiscountCodeTransaction();
        nTransaction.setClientTransactionId(transaction.getClientTransactionId());
        nTransaction.setTrxType(trxType);
        nTransaction.setDiscountAmount(transaction.getDiscountAmount());
        nTransaction.setOriginalAmount(transaction.getOriginalAmount());
        nTransaction.setApiUser(apiUser);
        nTransaction.setCustomer(nTransaction.getCustomer());
        nTransaction.setDiscountCode(transaction.getDiscountCode());
        nTransaction.setTransactionId(transaction.getTransactionId());
        nTransaction.setTrxDate(LocalDateTime.now());
        nTransaction.setStore(transaction.getStore());
        nTransaction.setCustomer(transaction.getCustomer());
        nTransaction.setRedeemTransaction(transaction);
        var savedTransaction = transactionRepository.save(nTransaction);

        transaction.setStatus(trxType == TransactionType.Confirmation ? TransactionStatus.Confirmed : 
                           trxType == TransactionType.Refund ? TransactionStatus.Refunded : TransactionStatus.Reversed);
        transactionRepository.save(transaction);

        var code = transaction.getDiscountCode();
        if(trxType == TransactionType.Confirmation) {
            logger.debug("Marking discount code {} as used", code.getCode());
            code.setUsed(code.getCurrentUsageCount() >= code.getUsageLimit());
            code.setActive(true);
            code.setRedeemDate(LocalDateTime.now());
        }
        else {
            logger.debug("Marking discount code {} as available", code.getCode());
            code.setUsed(false);
            code.setActive(true);
            code.setCurrentUsageCount(Math.max(0, code.getCurrentUsageCount() - 1));
        }
        codeRepository.save(code);

        logger.info("Successfully processed {} transaction: {}", trxType, savedTransaction.getTransactionId());
        return savedTransaction;
    }

    public DiscountCodeTransactionDto confirm(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating confirm transaction for discount code transaction: {}", dto.transactionId);
        return mapper.getFrom(settleTransaction(apiUser, dto, TransactionType.Confirmation));
    }

    public DiscountCodeTransactionDto reverse(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating reverse transaction for discount code transaction: {}", dto.transactionId);
        var transaction = settleTransaction(apiUser, dto, TransactionType.Reversal);

        return mapper.getFrom(transaction);
    }

    public DiscountCodeTransactionDto refund(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating refund transaction for discount code transaction: {}", dto.transactionId);
        var transaction = settleTransaction(apiUser, dto, TransactionType.Refund);

        return mapper.getFrom(transaction);
    }

    public List<DiscountCodeTransactionDto> getTransactions(int page, int size) {
        logger.info("Fetching discount code transactions with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DiscountCodeTransaction> transactionPage = transactionRepository.findAll(pageable);
        List<DiscountCodeTransaction> transactions = transactionPage.getContent();
        logger.info("Found {} discount code transactions", transactions.size());
        return mapper.getFrom(transactions);
    }
}
