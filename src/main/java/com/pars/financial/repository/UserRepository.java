package com.pars.financial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Find user by ID with store and company relationships eagerly fetched
     * @param id the user ID
     * @return user with relationships loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.store LEFT JOIN FETCH u.company WHERE u.id = :id")
    Optional<User> findByIdWithRelationships(@Param("id") Long id);
    
    /**
     * Find all users with store and company relationships eagerly fetched
     * @return list of all users with relationships loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.store LEFT JOIN FETCH u.company")
    List<User> findAllWithRelationships();
    
    /**
     * Find users by role ID with store and company relationships eagerly fetched
     * @param roleId the role ID
     * @return list of users with relationships loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.store LEFT JOIN FETCH u.company WHERE u.role.id = :roleId")
    List<User> findByRoleIdWithRelationships(@Param("roleId") Long roleId);
    
    /**
     * Find active users with store and company relationships eagerly fetched
     * @param isActive the active status
     * @return list of users with relationships loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.store LEFT JOIN FETCH u.company WHERE u.isActive = :isActive")
    List<User> findByIsActiveWithRelationships(@Param("isActive") boolean isActive);
} 