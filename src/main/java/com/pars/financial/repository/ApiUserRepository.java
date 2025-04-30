package com.pars.financial.repository;

import com.pars.financial.entity.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {
    public ApiUser findByApiKey(String apiKey);
}
