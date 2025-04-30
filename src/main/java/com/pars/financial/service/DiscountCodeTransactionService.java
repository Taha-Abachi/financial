package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.UUID;

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
        return mapper.getFrom(transactionRepository.findByTransactionId(transactionId));
    }

    public DiscountCodeTransactionDto redeem(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        var code = codeRepository.findByCode(dto.code);
        if(code == null)
            throw new ValidationException("Discount Code not found.");
        else if(!code.isActive())
            throw new ValidationException("Discount code is inactive.");
        else if(code.isUsed())
            throw new ValidationException("Discount already used.");
        
        var transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);
        if(transaction != null){
            throw new ValidationException("ClientTransactionId should be unique.");
        }
        
        var store = storeRepository.findById(dto.storeId);
        if (store.isEmpty()) {
            throw new ValidationException("Store Not Found");
        }
        var customer = customerRepository.findByPrimaryPhoneNumber(dto.phoneNo);
        if(customer == null){
            if (!autoDefineCustomer) {
                throw new CustomerNotFoundException("Customer not found");
            }
            else {
                customer = customerService.createCustomer(dto.phoneNo);
            }
        }

        var discount = (long) (code.getPercentage() * dto.originalAmount / 100);
        if(code.getMaxDiscountAmount() > 0) {
            discount = Math.min(discount, code.getMaxDiscountAmount());
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
        transactionRepository.save(transaction);

        code.setRedeemDate(LocalDateTime.now());
        code.setUsed(true);
        codeRepository.save(code);

        return mapper.getFrom(transaction);
    }

    public DiscountCodeTransaction settleTransaction(ApiUser apiUser, DiscountCodeTransactionDto dto, TransactionType trxType) {
        DiscountCodeTransaction transaction = null;
        if(trxType == TransactionType.Confirmation)
            transaction = transactionRepository.findByTransactionIdAndTrxType(dto.transactionId, TransactionType.Redeem);
        else if(trxType == TransactionType.Reversal)
            transaction = transactionRepository.findByClientTransactionIdAndTrxType(dto.clientTransactionId, TransactionType.Redeem);

        if(transaction == null)
            throw new ValidationException("Redeem Transaction Not Found.");

        var transaction2 = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Confirmation);
        if(transaction2 != null)
            throw new ValidationException("Transaction already confirmed.");
        transaction2 = transactionRepository.findByTransactionIdAndTrxType(transaction.getTransactionId(), TransactionType.Reversal);
        if(transaction2 != null)
            throw new ValidationException("Transaction already reversed.");

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
        transactionRepository.save(nTransaction);

        var code = transaction.getDiscountCode();
        if(trxType == TransactionType.Confirmation) {
            code.setUsed(true);
            code.setActive(true);
            code.setRedeemDate(LocalDateTime.now());
        }
        else {
            code.setUsed(false);
            code.setActive(true);
        }
        codeRepository.save(code);

        return nTransaction;
    }

    public DiscountCodeTransactionDto confirm(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        return mapper.getFrom(settleTransaction(apiUser, dto, TransactionType.Confirmation));
    }

    public DiscountCodeTransactionDto reverse(ApiUser apiUser, DiscountCodeTransactionDto dto) {
        var transaction = settleTransaction(apiUser, dto, TransactionType.Reversal);

        var code = transaction.getDiscountCode();
        code.setUsed(false);
        code.setRedeemDate(null);
        codeRepository.save(code);

        return mapper.getFrom(transaction);
    }
}
