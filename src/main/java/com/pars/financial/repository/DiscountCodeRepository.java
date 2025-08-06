package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.DiscountCode;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    public DiscountCode findByCode(String code);
    public boolean existsByCode(String code);
    public boolean existsBySerialNo(Long serialNo);
}
