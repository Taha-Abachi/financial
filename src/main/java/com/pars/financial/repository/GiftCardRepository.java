package com.pars.financial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.Company;
import com.pars.financial.entity.GiftCard;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    GiftCard findBySerialNo(String serialNo);
    GiftCard findByIdentifier(Long identifier);
    List<GiftCard> findByCompany(Company company);
}
