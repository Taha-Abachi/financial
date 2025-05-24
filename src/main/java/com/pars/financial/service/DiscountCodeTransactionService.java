package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.entity.DiscountCodeTransaction;
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
        var transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            logger.warn("Transaction not found: {}", transactionId);
        }
        return mapper.getFrom(transaction);
    }

    public DiscountCodeTransactionDto redeem(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        logger.info("Processing redeem request for discount code: {}, amount: {}, store: {}, phone: {}", 
            dto.code, dto.originalAmount, dto.storeId, dto.phoneNo);
            
        var code = codeRepository.findByCode(dto.code);
        if(code == null) {
            logger.warn("Discount code not found: {}", dto.code);
            throw new ValidationException("Discount Code not found.");
        }
        else if(!code.isActive()) {
            logger.warn("Discount code is inactive: {}", dto.code);
            throw new ValidationException("Discount code is inactive.");
        }
        else if(code.isUsed()) {
            logger.warn("Discount code already used: {}", dto.code);
            throw new ValidationException("Discount already used.");
        }
        
        if (code.getMinimumBillAmount() > 0 && dto.originalAmount < code.getMinimumBillAmount()) {
            logger.warn("Original amount {} is less than minimum bill amount {} for discount code: {}", 
                dto.originalAmount, code.getMinimumBillAmount(), dto.code);
            throw new ValidationException("Original amount is less than minimum bill amount required.");
        }
        
        var transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        if(transaction != null){
            logger.warn("Duplicate client transaction ID: {}", dto.clientTransactionId);
            throw new ValidationException("ClientTransactionId should be unique.");
        }
        
        var store = storeRepository.findById(dto.storeId);
        if (store.isEmpty()) {
            logger.warn("Store not found: {}", dto.storeId);
            throw new ValidationException("Store Not Found");
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

        var discount = (long) (code.getPercentage() * dto.originalAmount / 100);
        if(code.getMaxDiscountAmount() > 0) {
            discount = Math.min(discount, code.getMaxDiscountAmount());
        }
        logger.debug("Calculated discount amount: {} ({}% of {})", discount, code.getPercentage(), dto.originalAmount);

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
        var savedTransaction = transactionRepository.save(transaction);

        code.setRedeemDate(LocalDateTime.now());
        code.setUsed(true);
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

        if(transaction == null) {
            logger.warn("Redeem transaction not found for {}: {}", 
                (trxType == TransactionType.Confirmation ? "transactionId" : "clientTransactionId"),
                (trxType == TransactionType.Confirmation ? dto.transactionId : dto.clientTransactionId));
            throw new ValidationException("Redeem Transaction Not Found.");
        }

        var transaction2 = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Confirmation);
        if(transaction2 != null) {
            logger.warn("Transaction already confirmed: {}", transaction.getTransactionId());
            throw new ValidationException("Transaction already confirmed.");
        }
        
        transaction2 = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Reversal);
        if(transaction2 != null) {
            logger.warn("Transaction already reversed: {}", transaction.getTransactionId());
            throw new ValidationException("Transaction already reversed.");
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

        var code = transaction.getDiscountCode();
        if(trxType == TransactionType.Confirmation) {
            logger.debug("Marking discount code {} as used", code.getCode());
            code.setUsed(true);
            code.setActive(true);
            code.setRedeemDate(LocalDateTime.now());
        }
        else {
            logger.debug("Marking discount code {} as available", code.getCode());
            code.setUsed(false);
            code.setActive(true);
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

        var code = transaction.getDiscountCode();
        logger.debug("Resetting discount code {} after reversal", code.getCode());
        code.setUsed(false);
        code.setRedeemDate(null);
        codeRepository.save(code);

        return mapper.getFrom(transaction);
    }
}
