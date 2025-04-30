package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pars.financial.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
