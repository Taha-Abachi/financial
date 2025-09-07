package com.pars.financial.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pars.financial.dto.ItemCategoryDto;
import com.pars.financial.entity.ItemCategory;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.ItemCategoryRepository;

@Service
public class ItemCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryService.class);

    private final ItemCategoryRepository itemCategoryRepository;

    public ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
        this.itemCategoryRepository = itemCategoryRepository;
    }

    /**
     * Find item category by ID (only non-deleted)
     * @param categoryId the category ID
     * @return the category if found
     * @throws ValidationException if category not found
     */
    public ItemCategory findById(Long categoryId) {
        logger.debug("Finding item category by ID: {}", categoryId);
        Optional<ItemCategory> category = itemCategoryRepository.findByIdNonDeleted(categoryId);
        if (category.isEmpty()) {
            logger.warn("Item category not found with ID: {}", categoryId);
            throw new ValidationException("Item category not found", null, -200);
        }
        return category.get();
    }

    /**
     * Find item category by name
     * @param categoryName the category name
     * @return the category if found, null otherwise
     */
    public ItemCategory findByName(String categoryName) {
        logger.debug("Finding item category by name: {}", categoryName);
        return itemCategoryRepository.findByName(categoryName);
    }

    /**
     * Get all non-deleted item categories
     * @return list of all non-deleted item categories
     */
    public List<ItemCategory> getAllCategories() {
        logger.debug("Fetching all non-deleted item categories");
        return itemCategoryRepository.findAllNonDeleted();
    }

    /**
     * Get all non-deleted item categories as DTOs
     * @return list of all non-deleted item category DTOs
     */
    public List<ItemCategoryDto> getAllCategoryDtos() {
        logger.debug("Fetching all non-deleted item category DTOs");
        return itemCategoryRepository.findAllNonDeleted()
                .stream()
                .map(ItemCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all deleted item categories as DTOs
     * @return list of all deleted item category DTOs
     */
    public List<ItemCategoryDto> getAllDeletedCategoryDtos() {
        logger.debug("Fetching all deleted item category DTOs");
        return itemCategoryRepository.findAllDeleted()
                .stream()
                .map(ItemCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get item category by ID as DTO
     * @param categoryId the category ID
     * @return the category DTO if found
     * @throws ValidationException if category not found
     */
    public ItemCategoryDto getCategoryDtoById(Long categoryId) {
        ItemCategory category = findById(categoryId);
        return ItemCategoryDto.fromEntity(category);
    }

    /**
     * Create a new item category
     * @param category the category to create
     * @return the created category
     */
    public ItemCategory createCategory(ItemCategory category) {
        logger.info("Creating new item category: {}", category.getName());
        
        // Check if a category with this name exists (including deleted ones)
        ItemCategory existingCategory = itemCategoryRepository.findByNameIncludingDeleted(category.getName());
        if (existingCategory != null) {
            if (existingCategory.getIsDeleted()) {
                // If it's deleted, restore it instead of creating a new one
                logger.info("Restoring deleted category with name: {}", category.getName());
                existingCategory.setIsDeleted(false);
                existingCategory.setDescription(category.getDescription());
                return itemCategoryRepository.save(existingCategory);
            } else {
                throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
            }
        }
        
        category.setIsDeleted(false);
        return itemCategoryRepository.save(category);
    }

    /**
     * Create multiple item categories
     * @param categories list of categories to create
     * @return list of created categories
     */
    public List<ItemCategory> createCategories(List<ItemCategory> categories) {
        logger.info("Creating {} new item categories", categories.size());
        
        // Check for duplicate names and handle deleted categories
        for (ItemCategory category : categories) {
            ItemCategory existingCategory = itemCategoryRepository.findByNameIncludingDeleted(category.getName());
            if (existingCategory != null && !existingCategory.getIsDeleted()) {
                throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
            }
            category.setIsDeleted(false);
        }
        
        return itemCategoryRepository.saveAll(categories);
    }

    /**
     * Update an existing item category
     * @param category the category to update
     * @return the updated category
     */
    public ItemCategory updateCategory(ItemCategory category) {
        logger.info("Updating item category: {}", category.getName());
        if (category.getId() == null) {
            throw new ValidationException("Category ID is required for update", null, -202);
        }
        
        // Check if category exists (non-deleted)
        ItemCategory existingCategory = findById(category.getId());
        
        // Check if name is being changed and if new name already exists
        if (!existingCategory.getName().equals(category.getName()) && 
            itemCategoryRepository.existsByName(category.getName())) {
            throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
        }
        
        category.setIsDeleted(false);
        return itemCategoryRepository.save(category);
    }

    /**
     * Logical delete an item category
     * @param categoryId the category ID to delete
     * @param deleteUser the user performing the deletion
     */
    public void deleteCategory(Long categoryId, String deleteUser) {
        logger.info("Logically deleting item category with ID: {} by user: {}", categoryId, deleteUser);
        // Check if category exists (non-deleted)
        findById(categoryId);
        itemCategoryRepository.logicalDeleteByIdWithAudit(categoryId, java.time.LocalDateTime.now(), deleteUser);
    }

    /**
     * Logical delete an item category (without user info)
     * @param categoryId the category ID to delete
     */
    public void deleteCategory(Long categoryId) {
        logger.info("Logically deleting item category with ID: {}", categoryId);
        // Check if category exists (non-deleted)
        findById(categoryId);
        itemCategoryRepository.logicalDeleteById(categoryId);
    }

    /**
     * Restore a deleted item category
     * @param categoryId the category ID to restore
     */
    public void restoreCategory(Long categoryId) {
        logger.info("Restoring item category with ID: {}", categoryId);
        Optional<ItemCategory> category = itemCategoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new ValidationException("Category not found", null, -200);
        }
        if (!category.get().getIsDeleted()) {
            throw new ValidationException("Category is not deleted", null, -203);
        }
        itemCategoryRepository.restoreById(categoryId);
    }

    /**
     * Permanently delete an item category (physical delete)
     * @param categoryId the category ID to permanently delete
     */
    public void permanentDeleteCategory(Long categoryId) {
        logger.info("Permanently deleting item category with ID: {}", categoryId);
        Optional<ItemCategory> category = itemCategoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new ValidationException("Category not found", null, -200);
        }
        itemCategoryRepository.deleteById(categoryId);
    }

    /**
     * Check if category exists by name
     * @param categoryName the category name to check
     * @return true if category exists, false otherwise
     */
    public boolean existsByName(String categoryName) {
        return itemCategoryRepository.existsByName(categoryName);
    }

    /**
     * Deactivate an item category
     * @param categoryId the category ID to deactivate
     * @param deactiveUser the user performing the deactivation
     */
    public void deactivateCategory(Long categoryId, String deactiveUser) {
        logger.info("Deactivating item category with ID: {} by user: {}", categoryId, deactiveUser);
        // Check if category exists (non-deleted)
        findById(categoryId);
        itemCategoryRepository.deactivateById(categoryId, java.time.LocalDateTime.now());
    }

    /**
     * Activate an item category
     * @param categoryId the category ID to activate
     */
    public void activateCategory(Long categoryId) {
        logger.info("Activating item category with ID: {}", categoryId);
        // Check if category exists (non-deleted)
        findById(categoryId);
        itemCategoryRepository.activateById(categoryId);
    }

    /**
     * Get all inactive item categories as DTOs
     * @return list of all inactive item category DTOs
     */
    public List<ItemCategoryDto> getAllInactiveCategoryDtos() {
        logger.debug("Fetching all inactive item category DTOs");
        return itemCategoryRepository.findAllInactive()
                .stream()
                .map(ItemCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all item categories including inactive ones as DTOs
     * @return list of all non-deleted item category DTOs (including inactive)
     */
    public List<ItemCategoryDto> getAllCategoryDtosIncludingInactive() {
        logger.debug("Fetching all non-deleted item category DTOs including inactive");
        return itemCategoryRepository.findAllNonDeletedIncludingInactive()
                .stream()
                .map(ItemCategoryDto::fromEntity)
                .collect(Collectors.toList());
    }
} 