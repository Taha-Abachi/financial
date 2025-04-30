package com.pars.financial.repository;

import com.pars.financial.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    public DiscountCode findByCode(String code);
}
