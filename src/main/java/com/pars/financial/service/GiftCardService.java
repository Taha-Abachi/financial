package com.pars.financial.service;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.entity.Store;
import com.pars.financial.exception.GiftCardNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.mapper.GiftCardMapper;
import com.pars.financial.repository.GiftCardRepository;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.utils.RandomStringGenerator;
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

    final GiftCardRepository giftCardRepository;
    final GiftCardMapper giftCardMapper;
    final StoreRepository storeRepository;

    public GiftCardService(GiftCardRepository giftCardRepository, GiftCardMapper giftCardMapper, StoreRepository storeRepository) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardMapper = giftCardMapper;
        this.storeRepository = storeRepository;
    }

    private void validateRealAmount(long realAmount) {
        if (realAmount <= 0) {
            throw new ValidationException("Real amount must be greater than 0");
        }
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod) {
        validateRealAmount(realAmount);
        var gc = new GiftCard();
        gc.setIdentifier(ThreadLocalRandom.current().nextLong(10000000, 100000000));
//        gc.setIdentifier(Long.parseLong(RandomStringGenerator.generateRandomNumericString(8)));
        gc.setSerialNo("GC" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(8));
        gc.setInitialAmount(amount);
        gc.setRealAmount(realAmount);
        gc.setBalance(amount);
        gc.setIssueDate(LocalDate.now());
        gc.setExpiryDate(LocalDate.now().plusDays(validityPeriod));
        return gc;
    }

    public List<GiftCardDto> getGiftCards() {
        return giftCardMapper.getFrom(giftCardRepository.findAll());
    }

    public GiftCard getGiftCard(String serialNo) {
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if (gc == null) {
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }
        return gc;
    }

    public GiftCard getGiftCard(Long identifier) {
        var gc = giftCardRepository.findByIdentifier(identifier);
        if (gc == null) {
            throw new GiftCardNotFoundException("Gift Card Not Found with identifier: " + identifier);
        }
        return gc;
    }

    public GiftCardDto generateGiftCard(long realAmount, long amount, long validityPeriod) {
        return giftCardMapper.getFrom(giftCardRepository.save(issueGiftCard(realAmount, amount, validityPeriod)));
    }

    public List<GiftCardDto> generateGiftCards(long realAmount, long amount, long validityPeriod, int count) {
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < count; i++) {
            ls.add(issueGiftCard(realAmount, amount, validityPeriod));
        }
        giftCardRepository.saveAll(ls);
        return giftCardMapper.getFrom(ls);
    }

    @Transactional
    public void limitToStores(String serialNo, List<Long> storeIds) {
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            throw new GiftCardNotFoundException("Gift card not found");
        }

        if (storeIds == null || storeIds.isEmpty()) {
            giftCard.setStoreLimited(false);
            giftCard.setAllowedStores(new HashSet<>());
        } else {
            Set<Store> stores = new HashSet<>();
            for (Long storeId : storeIds) {
                var store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new ValidationException("Store not found with id: " + storeId));
                stores.add(store);
            }
            giftCard.setStoreLimited(true);
            giftCard.setAllowedStores(stores);
        }
        giftCardRepository.save(giftCard);
    }

    @Transactional
    public void removeStoreLimitation(String serialNo) {
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            throw new GiftCardNotFoundException("Gift card not found");
        }
        giftCard.setStoreLimited(false);
        giftCard.setAllowedStores(new HashSet<>());
        giftCardRepository.save(giftCard);
    }
}


