package com.pars.financial.repository;

import com.pars.financial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByMobilePhoneNumber(String mobilePhoneNumber);
    Optional<User> findByNationalCode(String nationalCode);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByMobilePhoneNumber(String mobilePhoneNumber);
    boolean existsByNationalCode(String nationalCode);
    boolean existsByEmail(String email);
    List<User> findByRoleId(Long roleId);
    List<User> findByIsActive(boolean isActive);
} 