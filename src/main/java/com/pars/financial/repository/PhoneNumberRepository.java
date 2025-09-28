package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.PhoneNumber;
import java.util.Optional;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
    
    /**
     * Find a phone number by its number value
     * @param number the phone number to search for
     * @return Optional containing the PhoneNumber if found, empty otherwise
     */
    Optional<PhoneNumber> findByNumber(String number);
}
