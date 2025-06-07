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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GiftCardService.class);

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
            logger.error("Invalid real amount: {}", realAmount);
            throw new ValidationException("Real amount must be greater than 0");
        }
    }

    private GiftCard issueGiftCard(long realAmount, long amount, long validityPeriod) {
        logger.debug("Issuing new gift card with realAmount: {}, amount: {}, validityPeriod: {}", realAmount, amount, validityPeriod);
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
        logger.debug("Created gift card with serialNo: {}", gc.getSerialNo());
        return gc;
    }

    public List<GiftCardDto> getGiftCards() {
        logger.debug("Fetching all gift cards");
        return giftCardMapper.getFrom(giftCardRepository.findAll());
    }

    public GiftCardDto getGiftCard(String serialNo) {
        logger.debug("Fetching gift card by serialNo: {}", serialNo);
        var gc = giftCardRepository.findBySerialNo(serialNo);
        if (gc == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift Card Not Found with serial No: " + serialNo);
        }
        return giftCardMapper.getFrom(gc);
    }

    public GiftCardDto getGiftCard(Long identifier) {
        logger.debug("Fetching gift card by identifier: {}", identifier);
        var gc = giftCardRepository.findByIdentifier(identifier);
        if (gc == null) {
            logger.warn("Gift card not found with identifier: {}", identifier);
            throw new GiftCardNotFoundException("Gift Card Not Found with identifier: " + identifier);
        }
        return giftCardMapper.getFrom(gc);
    }

    public GiftCardDto generateGiftCard(long realAmount, long amount, long validityPeriod) {
        logger.info("Generating new gift card with realAmount: {}, amount: {}, validityPeriod: {}", realAmount, amount, validityPeriod);
        var giftCard = issueGiftCard(realAmount, amount, validityPeriod);
        var savedCard = giftCardRepository.save(giftCard);
        logger.info("Generated gift card with serialNo: {}", savedCard.getSerialNo());
        return giftCardMapper.getFrom(savedCard);
    }

    public List<GiftCardDto> generateGiftCards(long realAmount, long amount, long validityPeriod, int count) {
        logger.info("Generating {} gift cards with realAmount: {}, amount: {}, validityPeriod: {}", count, realAmount, amount, validityPeriod);
        var ls = new ArrayList<GiftCard>();
        for (var i = 0; i < count; i++) {
            ls.add(issueGiftCard(realAmount, amount, validityPeriod));
        }
        var savedCards = giftCardRepository.saveAll(ls);
        logger.info("Generated {} gift cards successfully", count);
        return giftCardMapper.getFrom(savedCards);
    }

    @Transactional
    public void limitToStores(String serialNo, List<Long> storeIds) {
        logger.info("Limiting gift card {} to stores: {}", serialNo, storeIds);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }

        if (storeIds == null || storeIds.isEmpty()) {
            logger.debug("Removing store limitations for gift card: {}", serialNo);
            giftCard.setStoreLimited(false);
            giftCard.setAllowedStores(new HashSet<>());
        } else {
            Set<Store> stores = new HashSet<>();
            for (Long storeId : storeIds) {
                var store = storeRepository.findById(storeId)
                    .orElseThrow(() -> {
                        logger.warn("Store not found with id: {}", storeId);
                        return new ValidationException("Store not found with id: " + storeId);
                    });
                stores.add(store);
            }
            giftCard.setStoreLimited(true);
            giftCard.setAllowedStores(stores);
            logger.debug("Limited gift card {} to {} stores", serialNo, stores.size());
        }
        giftCardRepository.save(giftCard);
        logger.info("Successfully updated store limitations for gift card: {}", serialNo);
    }

    @Transactional
    public void removeStoreLimitation(String serialNo) {
        logger.info("Removing store limitations for gift card: {}", serialNo);
        var giftCard = giftCardRepository.findBySerialNo(serialNo);
        if (giftCard == null) {
            logger.warn("Gift card not found with serialNo: {}", serialNo);
            throw new GiftCardNotFoundException("Gift card not found");
        }
        giftCard.setStoreLimited(false);
        giftCard.setAllowedStores(new HashSet<>());
        giftCardRepository.save(giftCard);
        logger.info("Successfully removed store limitations for gift card: {}", serialNo);
    }
}


