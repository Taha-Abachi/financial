package com.pars.financial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.ItemCategory;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    
    /**
     * Find item category by name
     * @param name the category name to search for
     * @return the item category if found, null otherwise
     */
    ItemCategory findByName(String name);
    
    /**
     * Check if item category exists by name
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    boolean existsByName(String name);
} 