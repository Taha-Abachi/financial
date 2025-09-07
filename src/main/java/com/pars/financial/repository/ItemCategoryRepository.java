package com.pars.financial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pars.financial.entity.ItemCategory;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    
    /**
     * Find item category by name (only non-deleted)
     * @param name the category name to search for
     * @return the item category if found, null otherwise
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.name = :name AND ic.isDeleted = false")
    ItemCategory findByName(@Param("name") String name);
    
    /**
     * Find item category by name including deleted ones
     * @param name the category name to search for
     * @return the item category if found, null otherwise
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.name = :name")
    ItemCategory findByNameIncludingDeleted(@Param("name") String name);
    
    /**
     * Check if item category exists by name (only non-deleted)
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    @Query("SELECT COUNT(ic) > 0 FROM ItemCategory ic WHERE ic.name = :name AND ic.isDeleted = false")
    boolean existsByName(@Param("name") String name);
    
    /**
     * Check if item category exists by name including deleted ones
     * @param name the category name to check
     * @return true if category exists, false otherwise
     */
    @Query("SELECT COUNT(ic) > 0 FROM ItemCategory ic WHERE ic.name = :name")
    boolean existsByNameIncludingDeleted(@Param("name") String name);
    
    /**
     * Find all non-deleted and active item categories
     * @return list of non-deleted and active item categories
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.isDeleted = false AND ic.isActive = true")
    List<ItemCategory> findAllNonDeleted();
    
    /**
     * Find all non-deleted item categories (including inactive)
     * @return list of non-deleted item categories
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.isDeleted = false")
    List<ItemCategory> findAllNonDeletedIncludingInactive();
    
    /**
     * Find all deleted item categories
     * @return list of deleted item categories
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.isDeleted = true")
    List<ItemCategory> findAllDeleted();
    
    /**
     * Find all inactive item categories (non-deleted but inactive)
     * @return list of inactive item categories
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.isDeleted = false AND ic.isActive = false")
    List<ItemCategory> findAllInactive();
    
    /**
     * Find item category by ID (only non-deleted)
     * @param id the category ID
     * @return the item category if found and not deleted
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.id = :id AND ic.isDeleted = false")
    Optional<ItemCategory> findByIdNonDeleted(@Param("id") Long id);
    
    /**
     * Logical delete - mark category as deleted
     * @param id the category ID to delete
     */
    @Modifying
    @Query("UPDATE ItemCategory ic SET ic.isDeleted = true WHERE ic.id = :id")
    void logicalDeleteById(@Param("id") Long id);
    
    /**
     * Restore deleted category
     * @param id the category ID to restore
     */
    @Modifying
    @Query("UPDATE ItemCategory ic SET ic.isDeleted = false, ic.deleteDate = NULL, ic.deleteUser = NULL WHERE ic.id = :id")
    void restoreById(@Param("id") Long id);
    
    /**
     * Deactivate category (set isActive = false)
     * @param id the category ID to deactivate
     * @param deactiveDate the deactivation date
     * @param deleteUser the user who deactivated
     */
    @Modifying
    @Query("UPDATE ItemCategory ic SET ic.isActive = false, ic.deactiveDate = :deactiveDate WHERE ic.id = :id")
    void deactivateById(@Param("id") Long id, @Param("deactiveDate") java.time.LocalDateTime deactiveDate);
    
    /**
     * Activate category (set isActive = true)
     * @param id the category ID to activate
     */
    @Modifying
    @Query("UPDATE ItemCategory ic SET ic.isActive = true, ic.deactiveDate = NULL WHERE ic.id = :id")
    void activateById(@Param("id") Long id);
    
    /**
     * Logical delete with audit information
     * @param id the category ID to delete
     * @param deleteDate the deletion date
     * @param deleteUser the user who deleted
     */
    @Modifying
    @Query("UPDATE ItemCategory ic SET ic.isDeleted = true, ic.deleteDate = :deleteDate, ic.deleteUser = :deleteUser WHERE ic.id = :id")
    void logicalDeleteByIdWithAudit(@Param("id") Long id, @Param("deleteDate") java.time.LocalDateTime deleteDate, @Param("deleteUser") String deleteUser);
} 