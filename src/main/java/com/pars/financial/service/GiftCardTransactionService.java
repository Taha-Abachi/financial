package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pars.financial.dto.GiftCardTransactionDto;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.enums.TransactionStatus;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.exception.ClientTransactionIdNotUniqueException;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.GenericException;
import com.pars.financial.exception.GiftCardNotFoundException;
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
                throw new ValidationException("Gift card cannot be used in this store", null, -117);
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

    public GiftCardTransactionDto debitGiftCard(ApiUser user, String clientTransactionId, long amount, String serialNo, Long storeId, String phoneNumber) {
        logger.info("Processing debit request for gift card: {}, amount: {}, store: {}, phone: {}", serialNo, amount, storeId, phoneNumber);
        
        if((phoneNumber == null) || (phoneNumber.length() != 11)) {
            logger.warn("Invalid phone number: {}", phoneNumber);
            throw new ValidationException("Customer Phone Number is incorrect", null, -118);
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
            throw new ValidationException("Store Not Found", null, -119);
        }

        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc != null){
            validateStoreLimit(gc, storeId);
            validateCompanyAccess(gc, storeId);
            if(gc.getBalance() >= amount) {
                logger.debug("Processing debit transaction for gift card: {}, current balance: {}, debit amount: {}", 
                    gc.getSerialNo(), gc.getBalance(), amount);
                
                GiftCardTransaction transaction = new GiftCardTransaction();
                transaction.setTransactionType(TransactionType.Debit);
                transaction.setGiftCard(gc);
                transaction.setCustomer(customer);
                transaction.setApiUser(user);
                transaction.setStore(store.get());
                transaction.setAmount(amount);
                transaction.setBalanceBefore(gc.getBalance());
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

    public GiftCardTransactionDto settleTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId, TransactionType trxType) {
        logger.info("Processing {} transaction for gift card: {}, amount: {}, transactionId: {}", 
            trxType, serialNo, amount, transactionId);
            
        if(amount <= 0){
            logger.warn("Invalid amount: {}", amount);
            throw new ValidationException("Amount must be greater than 0.", null, -122);
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
            throw new ValidationException("Debit Transaction Not Found.", null, -123);
        } else if (amount != debitTransaction.getAmount()) {    
            logger.warn("Amount mismatch. Expected: {}, Actual: {}", debitTransaction.getAmount(), amount);
            throw new ValidationException("Debit Amount Not Matched.", null, -124);
        } else if (!Objects.equals(debitTransaction.getGiftCard().getId(), gc.getId())) {
            logger.warn("Gift card mismatch. Expected: {}, Actual: {}", 
                debitTransaction.getGiftCard().getSerialNo(), gc.getSerialNo());
            throw new ValidationException("Debit Gift Card Not Matched.", null, -125);
        }

        var confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
        var reversal = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());

        // Validate transaction state
        switch (trxType) {
            case Credit, Debit, Redeem -> {
                logger.warn("Invalid transaction type: {}", trxType);
                throw new ValidationException("Invalid Transaction", null, -126);
            }
            case Reversal, Confirmation -> {
                if (confirmation != null) {
                    logger.warn("Transaction already confirmed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already confirmed.", null, -127);
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already reversed.", null, -128);
                }
            }
            case Refund -> {
                var refund = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, debitTransaction.getTransactionId());
                if (refund != null) {
                    logger.warn("Transaction already refunded: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already refunded.", null, -129);
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already reversed.", null, -130);
                }
                if(confirmation == null) {
                    logger.warn("Transaction not confirmed yet: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction not Confirmed yet.", null, -131);
                }
            }
        }

        if(((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) && (gc.getInitialAmount() < gc.getBalance() + amount)) {
            logger.warn("Amount error. Initial: {}, Current: {}, Add: {}", 
                gc.getInitialAmount(), gc.getBalance(), amount);
            throw new ValidationException("Amount Error.", null, -132); 
        }

        logger.debug("Creating {} transaction for debit transaction: {}", trxType, debitTransaction.getTransactionId());
        GiftCardTransaction transaction = new GiftCardTransaction();
        transaction.setCustomer(debitTransaction.getCustomer());
        transaction.setApiUser(user);
        transaction.setDebitTransaction(debitTransaction);
        transaction.setClientTransactionId(debitTransaction.getClientTransactionId());
        transaction.setTransactionId(debitTransaction.getTransactionId());
        transaction.setTransactionType(trxType);
        transaction.setDebitTransaction(debitTransaction);
        transaction.setGiftCard(gc);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(gc.getBalance());
        transaction.setTrxDate(LocalDateTime.now());
        transaction.setStore(debitTransaction.getStore());
        
        if ((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) {
            gc.setBalance(gc.getBalance() + amount);
        }
        gc.setLastUsed(LocalDateTime.now());
        
        giftCardRepository.save(gc);
        var savedTransaction = transactionRepository.save(transaction);
        
        switch (trxType) {
            case Reversal -> {
                debitTransaction.setStatus(TransactionStatus.Reversed);
            }
            case Refund -> {
                debitTransaction.setStatus(TransactionStatus.Refunded);
            }
            case Confirmation -> {
                debitTransaction.setStatus(TransactionStatus.Confirmed);
            }
        }
        transactionRepository.save(debitTransaction);
        
        logger.info("Successfully processed {} transaction: {}, new balance: {}", 
            trxType, savedTransaction.getTransactionId(), gc.getBalance());
            
        return giftCardTransactionMapper.getFrom(savedTransaction);
    }

    public GiftCardTransactionDto reverseTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        logger.info("Initiating reverse transaction for gift card: {}, amount: {}", serialNo, amount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Reversal);
    }

    public GiftCardTransactionDto confirmTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        logger.info("Initiating confirm transaction for gift card: {}, amount: {}", serialNo, amount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Confirmation);
    }

    public GiftCardTransactionDto refundTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        logger.info("Initiating refund transaction for gift card: {}, amount: {}", serialNo, amount);
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Refund);
    }

    public GiftCardTransactionDto get(String trxId) {
        logger.debug("Fetching transaction details for ID: {}", trxId);
        return giftCardTransactionMapper.getFrom(transactionRepository.findByTransactionId(UUID.fromString(trxId)));
    }

    public GiftCardTransactionDto checkStatus(String clientTransactionId) {
        logger.debug("Checking transaction status for clientTransactionId: {}", clientTransactionId);
        return giftCardTransactionMapper.getFrom(transactionRepository.findByClientTransactionId(clientTransactionId));
    }

    public List<GiftCardTransactionDto> getTransactionHistory(String serialNo) {
        logger.debug("Fetching transaction history for gift card: {}", serialNo);
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if(gc != null) {
            return giftCardTransactionMapper.getFrom(transactionRepository.findByGiftCardAndTransactionType(gc, TransactionType.Debit));
        }
        logger.warn("Gift card not found: {}", serialNo);
        throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
    }

    /**
     * Get all gift cards for a specific company
     * @param companyId the company ID
     * @return list of gift card transaction DTOs
     */
    public List<GiftCardTransactionDto> getGiftCardsByCompany(Long companyId) {
        logger.debug("Fetching gift cards for company: {}", companyId);
        var company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            logger.warn("Company not found with ID: {}", companyId);
            throw new ValidationException("Company not found", null, -134);
        }
        
        var giftCards = giftCardRepository.findByCompany(company.get());
        return giftCards.stream()
            .map(gc -> {
                // Get the latest transaction for each gift card
                var latestTransaction = transactionRepository.findTopByGiftCardOrderByTrxDateDesc(gc);
                return latestTransaction != null ? giftCardTransactionMapper.getFrom(latestTransaction) : null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Get transaction history for all gift cards of a company
     * @param companyId the company ID
     * @return list of gift card transaction DTOs
     */
    public List<GiftCardTransactionDto> getCompanyTransactionHistory(Long companyId) {
        logger.debug("Fetching transaction history for company: {}", companyId);
        var company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            logger.warn("Company not found with ID: {}", companyId);
            throw new ValidationException("Company not found", null, -134);
        }
        
        var giftCards = giftCardRepository.findByCompany(company.get());
        return giftCards.stream()
            .flatMap(gc -> transactionRepository.findByGiftCard(gc).stream())
            .map(giftCardTransactionMapper::getFrom)
            .toList();
    }
}
