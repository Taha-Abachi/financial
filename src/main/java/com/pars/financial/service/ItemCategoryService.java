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
     * Find item category by ID
     * @param categoryId the category ID
     * @return the category if found
     * @throws ValidationException if category not found
     */
    public ItemCategory findById(Long categoryId) {
        logger.debug("Finding item category by ID: {}", categoryId);
        Optional<ItemCategory> category = itemCategoryRepository.findById(categoryId);
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
     * Get all item categories
     * @return list of all item categories
     */
    public List<ItemCategory> getAllCategories() {
        logger.debug("Fetching all item categories");
        return itemCategoryRepository.findAll();
    }

    /**
     * Get all item categories as DTOs
     * @return list of all item category DTOs
     */
    public List<ItemCategoryDto> getAllCategoryDtos() {
        logger.debug("Fetching all item category DTOs");
        return itemCategoryRepository.findAll()
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
        if (itemCategoryRepository.existsByName(category.getName())) {
            throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
        }
        return itemCategoryRepository.save(category);
    }

    /**
     * Create multiple item categories
     * @param categories list of categories to create
     * @return list of created categories
     */
    public List<ItemCategory> createCategories(List<ItemCategory> categories) {
        logger.info("Creating {} new item categories", categories.size());
        
        // Check for duplicate names
        for (ItemCategory category : categories) {
            if (itemCategoryRepository.existsByName(category.getName())) {
                throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
            }
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
        
        // Check if category exists
        if (!itemCategoryRepository.existsById(category.getId())) {
            throw new ValidationException("Category not found", null, -200);
        }
        
        // Check if name is being changed and if new name already exists
        ItemCategory existingCategory = itemCategoryRepository.findById(category.getId()).get();
        if (!existingCategory.getName().equals(category.getName()) && 
            itemCategoryRepository.existsByName(category.getName())) {
            throw new ValidationException("Category with name '" + category.getName() + "' already exists", null, -201);
        }
        
        return itemCategoryRepository.save(category);
    }

    /**
     * Delete an item category
     * @param categoryId the category ID to delete
     */
    public void deleteCategory(Long categoryId) {
        logger.info("Deleting item category with ID: {}", categoryId);
        if (!itemCategoryRepository.existsById(categoryId)) {
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
} 