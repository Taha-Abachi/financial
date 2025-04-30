package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        if (giftCard.isStoreLimited()) {
            boolean storeAllowed = giftCard.getAllowedStores().stream()
                .anyMatch(store -> store.getId().equals(storeId));
            if (!storeAllowed) {
                throw new ValidationException("Gift card cannot be used in this store");
            }
        }
    }

    public GiftCardTransactionDto debitGiftCard(ApiUser user, String clientTransactionId, long amount, String serialNo, Long storeId, String phoneNumber) {
        if((phoneNumber == null) || (phoneNumber.length() != 11)) {
            throw new ValidationException("Customer Phone Number is incorrect");
        }
        var customer = customerRepository.findByPrimaryPhoneNumber(phoneNumber);
        if(customer == null) {
            if (!autoDefineCustomer) {
                throw new CustomerNotFoundException("Customer not found");
            }
            else {
                customer = customerService.createCustomer(phoneNumber);
            }
        }
        var transactionHistory = giftCardTransactionRepository.findByTransactionTypeAndClientTransactionId(TransactionType.Debit, clientTransactionId);
        if (transactionHistory != null) {
            throw new ClientTransactionIdNotUniqueException("Client Transaction Id Not Unique");
        }
        var store = storeRepository.findById(storeId);
        if (store.isEmpty()) {
            throw new ValidationException("Store Not Found");
        }
        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc != null){
            validateStoreLimit(gc, storeId);
            if(gc.getBalance() >= amount) {
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
                transactionRepository.save(transaction);
                return giftCardTransactionMapper.getFrom(transaction);
            }
            throw new GenericException("Not enough balance");
        }
        throw new GenericException("General Failure");
    }

    public GiftCardTransactionDto settleTransaction(ApiUser user,  String clientTransactionId, long amount, String serialNo, UUID transactionId, TransactionType trxType) {
        if(amount <= 0){
            throw new ValidationException("Amount must be greater than 0.");
        }
        var gc = giftCardRepository.findBySerialNo(serialNo.toUpperCase());
        if (gc == null) {
            throw new GiftCardNotFoundException("Gift Card Not Found.");
        }
        GiftCardTransaction debitTransaction;
        if (trxType == TransactionType.Reversal)
            debitTransaction = transactionRepository.findByTransactionTypeAndClientTransactionId(TransactionType.Debit, clientTransactionId);
        else
            debitTransaction = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Debit, transactionId);
        if (debitTransaction == null) {
            throw new ValidationException("Debit Transaction Not Found.");
        } else if (amount != debitTransaction.getAmount()) {
            throw new ValidationException("Debit Amount Not Matched.");
        } else if (!Objects.equals(debitTransaction.getGiftCard().getId(), gc.getId())) {
            throw new ValidationException("Debit Gift Card Not Matched.");
        }

        var confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
        var reversal = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());


        switch (trxType) {
            case Credit, Debit, Redeem -> {
                throw new ValidationException("Invalid Transaction");
            }
            case Reversal, Confirmation -> {
                if (confirmation != null) {
                    throw new ValidationException("Transaction already confirmed.");
                }
                if(reversal != null)
                    throw new ValidationException("Transaction already reversed.");
            }
            case Refund -> {
                var refund = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Refund, debitTransaction.getTransactionId());
                if (refund != null) {
                    throw new ValidationException("Transaction already refunded.");
                }
                if(reversal != null)
                    throw new ValidationException("Transaction already reversed.");
                if(confirmation == null)
                    throw new ValidationException("Transaction not Confirmed yet.");
            }
        }

//        var transaction2 = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
//        if (transaction2 != null) {
//            throw new ValidationException("Transaction already confirmed.");
//        }
//        transaction2 = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());
//        if(transaction2 != null)
//            throw new ValidationException("Transaction already reversed.");
//
//        GiftCardTransaction confirmation = null;
//        switch (trxType) {
//            case Debit, Credit, Redeem -> {
//                break;
//            }
//            case Confirmation -> {
//                confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());
//                if (confirmation != null) {
//                    throw new ValidationException("Transaction is already reversed.", null, -104);
//                }
//            }
//            case Reversal -> {
//                confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
//                if (confirmation != null) {
//                    throw new ValidationException("Transaction is already confirmed.", null, -105);
//                }
//            }
//            case Refund -> {
//                confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Reversal, debitTransaction.getTransactionId());
//                if (confirmation != null) {
//                    throw new ValidationException("Transaction is already reversed.", null, -104);
//                }
//                confirmation = transactionRepository.findByTransactionTypeAndTransactionId(TransactionType.Confirmation, debitTransaction.getTransactionId());
//                if (confirmation == null) {
//                    throw new ValidationException("Transaction is not yet confirmed.", null, -106);
//                }
//            }
//            default -> {
//                break;
//}
//        }
        if(((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund)) && (gc.getInitialAmount() < gc.getBalance() + amount)) {
            throw new ValidationException("Amount Error.");
        }
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
        if ((trxType == TransactionType.Reversal) || (trxType == TransactionType.Refund))
            gc.setBalance(gc.getBalance() + amount);
        gc.setLastUsed(LocalDateTime.now());
        giftCardRepository.save(gc);
        transactionRepository.save(transaction);
        return giftCardTransactionMapper.getFrom(transaction);
    }

    public GiftCardTransactionDto reverseTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Reversal);
    }

    public GiftCardTransactionDto confirmTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Confirmation);
    }

    public GiftCardTransactionDto refundTransaction(ApiUser user, String clientTransactionId, long amount, String serialNo, UUID transactionId) {
        return settleTransaction(user, clientTransactionId, amount, serialNo, transactionId, TransactionType.Refund);
    }

    public GiftCardTransactionDto get(String trxId) {
        return giftCardTransactionMapper.getFrom(transactionRepository.findByTransactionId(UUID.fromString(trxId)));
    }

    public GiftCardTransactionDto checkStatus(String clientTransactionId) {
        return giftCardTransactionMapper.getFrom(transactionRepository.findByClientTransactionId(clientTransactionId));
    }

    public List<GiftCardTransactionDto> getTransactionHistory(String serialNo) {
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if(gc != null) {
            return giftCardTransactionMapper.getFrom(transactionRepository.findByGiftCardAndTransactionType(gc, TransactionType.Debit));
        }
        throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
    }
}
