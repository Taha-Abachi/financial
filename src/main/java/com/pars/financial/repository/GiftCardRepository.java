package com.pars.financial.repository;

import com.pars.financial.entity.GiftCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    GiftCard findBySerialNo(String serialNo);
    GiftCard findByIdentifier(Long identifier);
}
