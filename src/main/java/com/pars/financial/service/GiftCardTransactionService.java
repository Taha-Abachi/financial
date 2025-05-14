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
import com.pars.financial.enums.TransactionType;
import com.pars.financial.exception.ClientTransactionIdNotUniqueException;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.GenericException;
import com.pars.financial.exception.GiftCardNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.GiftCardTransactionMapper;
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

    private final CustomerService customerService;

    final GiftCardTransactionMapper giftCardTransactionMapper;

    public GiftCardTransactionService(GiftCardRepository giftCardRepository, GiftCardTransactionRepository transactionRepository, StoreRepository storeRpository, CustomerRepository customerRepository, GiftCardTransactionMapper giftCardTransactionMapper, GiftCardTransactionRepository giftCardTransactionRepository, CustomerService customerService) {
        this.giftCardRepository = giftCardRepository;
        this.transactionRepository = transactionRepository;
        this.storeRepository = storeRpository;
        this.customerRepository = customerRepository;
        this.giftCardTransactionMapper = giftCardTransactionMapper;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.customerService = customerService;
    }

    private void validateStoreLimit(GiftCard giftCard, Long storeId) {
        logger.debug("Validating store limit for gift card: {} and store: {}", giftCard.getSerialNo(), storeId);
        if (giftCard.isStoreLimited()) {
            boolean storeAllowed = giftCard.getAllowedStores().stream()
                .anyMatch(store -> store.getId().equals(storeId));
            if (!storeAllowed) {
                logger.warn("Store {} not allowed for gift card {}", storeId, giftCard.getSerialNo());
                throw new ValidationException("Gift card cannot be used in this store");
            }
        }
    }

    public GiftCardTransactionDto debitGiftCard(ApiUser user, String clientTransactionId, long amount, String serialNo, Long storeId, String phoneNumber) {
        logger.info("Processing debit request for gift card: {}, amount: {}, store: {}, phone: {}", serialNo, amount, storeId, phoneNumber);
        
        if((phoneNumber == null) || (phoneNumber.length() != 11)) {
            logger.warn("Invalid phone number: {}", phoneNumber);
            throw new ValidationException("Customer Phone Number is incorrect");
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
            throw new ValidationException("Store Not Found");
        }

        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc != null){
            validateStoreLimit(gc, storeId);
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
            throw new GenericException("Not enough balance");
        }
        logger.error("Gift card not found: {}", serialNo);
        throw new GenericException("General Failure");
    }

    public GiftCardTransactionDto settleTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId, TransactionType trxType) {
        logger.info("Processing {} transaction for gift card: {}, amount: {}, transactionId: {}", 
            trxType, serialNo, amount, transactionId);
            
        if(amount <= 0){
            logger.warn("Invalid amount: {}", amount);
            throw new ValidationException("Amount must be greater than 0.");
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
            throw new ValidationException("Debit Transaction Not Found.");
        } else if (amount != debitTransaction.getAmount()) {
            logger.warn("Amount mismatch. Expected: {}, Actual: {}", debitTransaction.getAmount(), amount);
            throw new ValidationException("Debit Amount Not Matched.");
        } else if (!Objects.equals(debitTransaction.getGiftCard().getId(), gc.getId())) {
            logger.warn("Gift card mismatch. Expected: {}, Actual: {}", 
                debitTransaction.getGiftCard().getSerialNo(), gc.getSerialNo());
            throw new ValidationException("Debit Gift Card Not Matched.");
        }

        var confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
        var reversal = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());

        // Validate transaction state
        switch (trxType) {
            case Credit, Debit, Redeem -> {
                logger.warn("Invalid transaction type: {}", trxType);
                throw new ValidationException("Invalid Transaction");
            }
            case Reversal, Confirmation -> {
                if (confirmation != null) {
                    logger.warn("Transaction already confirmed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already confirmed.");
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already reversed.");
                }
            }
            case Refund -> {
                var refund = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, debitTransaction.getTransactionId());
                if (refund != null) {
                    logger.warn("Transaction already refunded: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already refunded.");
                }
                if(reversal != null) {
                    logger.warn("Transaction already reversed: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction already reversed.");
                }
                if(confirmation == null) {
                    logger.warn("Transaction not confirmed yet: {}", debitTransaction.getTransactionId());
                    throw new ValidationException("Transaction not Confirmed yet.");
                }
            }
        }

        if(((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) && (gc.getInitialAmount() < gc.getBalance() + amount)) {
            logger.warn("Amount error. Initial: {}, Current: {}, Add: {}", 
                gc.getInitialAmount(), gc.getBalance(), amount);
            throw new ValidationException("Amount Error.");
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
}
