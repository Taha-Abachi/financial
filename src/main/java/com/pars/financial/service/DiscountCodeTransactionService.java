package com.pars.financial.service;

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
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.entity.User;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.DiscountCodeTransactionMapper;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.repository.DiscountCodeTransactionRepository;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.utils.PersianErrorMessages;

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
    private final DiscountCodeService discountCodeService;

    private final DiscountCodeTransactionMapper mapper;

    public DiscountCodeTransactionService(DiscountCodeRepository discountCodeRepository, DiscountCodeTransactionRepository discountCodeTransactionRepository, CustomerRepository customerRepository, StoreRepository storeRepository, CustomerService customerService, DiscountCodeTransactionMapper mapper, DiscountCodeService discountCodeService) {
        this.codeRepository = discountCodeRepository;
        this.transactionRepository = discountCodeTransactionRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.customerService = customerService;
        this.discountCodeService = discountCodeService;
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

    public DiscountCodeTransactionDto redeem(User apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Processing redeem request for discount code: {}, amount: {}, store: {}, phone: {}", 
            dto.code, dto.originalAmount, dto.storeId, dto.phoneNo);
            
        // Use shared validation method
        var validationResult = discountCodeService.validateDiscountCodeRules(dto);
        if (!validationResult.isValid) {
            logger.warn("Discount code validation failed: {}", validationResult.message);
            throw new ValidationException(validationResult.message, null, getErrorCode(validationResult.errorCode));
        }
        
        // Check for duplicate client transaction ID
        var transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        if(transaction != null){
            logger.warn("Duplicate client transaction ID: {}", dto.clientTransactionId);
            throw new ValidationException("ClientTransactionId should be unique.", PersianErrorMessages.DUPLICATE_TRANSACTION_ID, null, -109);
        }
        
        var code = codeRepository.findByCode(dto.code);
        var store = storeRepository.findById(dto.storeId).get();
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

        // Create and save transaction
        transaction = new DiscountCodeTransaction();
        transaction.setDiscountCode(code);
        transaction.setTrxType(TransactionType.Redeem);
        transaction.setClientTransactionId(dto.clientTransactionId);
        transaction.setDiscountAmount(validationResult.calculatedDiscountAmount);
        transaction.setOriginalAmount(dto.originalAmount);
        transaction.setApiUser(apiUser);
        transaction.setCustomer(customer);
        transaction.setStore(store);
        transaction.setTrxDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.Pending);
        var savedTransaction = transactionRepository.save(transaction);

        // Update discount code usage
        code.setRedeemDate(LocalDateTime.now());
        code.setUsed(code.getCurrentUsageCount() + 1 >= code.getUsageLimit());
        code.setCurrentUsageCount(code.getCurrentUsageCount() + 1);
        codeRepository.save(code);

        logger.info("Successfully processed redeem transaction: {}, discount amount: {}", 
            savedTransaction.getTransactionId(), validationResult.calculatedDiscountAmount);
        return mapper.getFrom(savedTransaction);
    }
    
    /**
     * Helper method to convert error codes to numeric values
     */
    private int getErrorCode(String errorCode) {
        return switch (errorCode) {
            case "DISCOUNT_CODE_NOT_FOUND" -> -104;
            case "DISCOUNT_CODE_EXPIRED" -> -141;
            case "DISCOUNT_CODE_INACTIVE" -> -105;
            case "DISCOUNT_CODE_ALREADY_USED" -> -106;
            case "DISCOUNT_CODE_USAGE_LIMIT_REACHED" -> -107;
            case "MINIMUM_BILL_AMOUNT_NOT_MET" -> -108;
            case "STORE_NOT_FOUND" -> -110;
            case "STORE_NOT_ALLOWED" -> -117;
            default -> -1;
        };
    }

    public DiscountCodeTransaction settleTransaction(User apiUser, DiscountCodeTransactionDto dto, TransactionType trxType) {
        logger.info("Processing {} transaction for discount code transaction: {}", trxType, dto.transactionId);
        
        DiscountCodeTransaction transaction = null;
        if(null != trxType) switch (trxType) {
            case Confirmation -> {
                logger.debug("Looking up redeem transaction by transactionId: {}", dto.transactionId);
                transaction = transactionRepository.findByTransactionIdAndTrxType(dto.transactionId, TransactionType.Redeem);
            }
            case Reversal -> {
                logger.debug("Looking up redeem transaction by clientTransactionId: {}", dto.clientTransactionId);
                transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
            }
            case Refund -> {
                logger.debug("Looking up redeem transaction by transactionId: {}", dto.transactionId);
                transaction = transactionRepository.findByTransactionIdAndTrxType(dto.transactionId, TransactionType.Redeem);
            }
            default -> {
            }
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

    public DiscountCodeTransactionDto confirm(User apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating confirm transaction for discount code transaction: {}", dto.transactionId);
        return mapper.getFrom(settleTransaction(apiUser, dto, TransactionType.Confirmation));
    }

    public DiscountCodeTransactionDto reverse(User apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating reverse transaction for discount code transaction: {}", dto.transactionId);
        var transaction = settleTransaction(apiUser, dto, TransactionType.Reversal);

        return mapper.getFrom(transaction);
    }

    public DiscountCodeTransactionDto refund(User apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Initiating refund transaction for discount code transaction: {}", dto.transactionId);
        var transaction = settleTransaction(apiUser, dto, TransactionType.Refund);

        return mapper.getFrom(transaction);
    }

    /**
     * Check discount code validity without affecting the database
     * Returns the same DTO structure as redeem but with null transactionId
     */
    public DiscountCodeTransactionDto checkDiscountCode(User apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Checking discount code: {} for amount: {}, store: {}, phone: {}", 
            dto.code, dto.originalAmount, dto.storeId, dto.phoneNo);
            
        // Use shared validation method
        var validationResult = discountCodeService.validateDiscountCodeRules(dto);
        if (!validationResult.isValid) {
            logger.warn("Discount code validation failed: {}", validationResult.message);
            throw new ValidationException(validationResult.message, null, getErrorCode(validationResult.errorCode));
        }
        
        // Check for duplicate client transaction ID
        var transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        if(transaction != null){
            logger.warn("Duplicate client transaction ID: {}", dto.clientTransactionId);
            throw new ValidationException("ClientTransactionId should be unique.", PersianErrorMessages.DUPLICATE_TRANSACTION_ID, null, -109);
        }
        
        var store = storeRepository.findById(dto.storeId).get();
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

        // Create response DTO with same structure as redeem but no database changes
        var responseDto = new DiscountCodeTransactionDto();
        responseDto.code = dto.code;
        responseDto.originalAmount = dto.originalAmount;
        responseDto.discountAmount = validationResult.calculatedDiscountAmount;
        responseDto.percentage = validationResult.percentage;
        responseDto.maxDiscountAmount = validationResult.maxDiscountAmount;
        responseDto.discountType = validationResult.discountType;
        responseDto.trxType = TransactionType.Redeem;
        responseDto.status = TransactionStatus.Pending;
        responseDto.storeId = dto.storeId;
        responseDto.phoneNo = dto.phoneNo;
        responseDto.clientTransactionId = dto.clientTransactionId;
        responseDto.storeName = store.getStore_name();
        responseDto.orderno = dto.orderno;
        responseDto.description = dto.description;
        
        // Set transactionId to null for check operations
        responseDto.transactionId = null;
        
        logger.info("Successfully checked discount code: {}, calculated discount amount: {}", 
            dto.code, validationResult.calculatedDiscountAmount);
        return responseDto;
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
