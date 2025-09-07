package com.pars.financial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByMobilePhoneNumber(String mobilePhoneNumber);
    Optional<User> findByNationalCode(String nationalCode);
    Optional<User> findByEmail(String email);
    Optional<User> findByApiKey(String apiKey);
    boolean existsByUsername(String username);
    boolean existsByMobilePhoneNumber(String mobilePhoneNumber);
    boolean existsByNationalCode(String nationalCode);
    boolean existsByEmail(String email);
    boolean existsByApiKey(String apiKey);
    List<User> findByRoleId(Long roleId);
    List<User> findByIsActive(boolean isActive);
    long countByIsActive(boolean isActive);
} 