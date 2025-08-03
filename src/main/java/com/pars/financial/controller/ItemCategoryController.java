package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.ItemCategoryDto;
import com.pars.financial.entity.ApiUser;
import com.pars.financial.entity.ItemCategory;
import com.pars.financial.service.ItemCategoryService;
import com.pars.financial.utils.ApiUserUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/item-category")
public class ItemCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryController.class);
    final ItemCategoryService itemCategoryService;

    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @GetMapping("/list")
    public GenericResponse<List<ItemCategoryDto>> getAllCategories() {
        logger.info("GET /api/v1/item-category/list called");
        var res = new GenericResponse<List<ItemCategoryDto>>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            var categories = itemCategoryService.getAllCategoryDtos();
            res.data = categories;
        } catch (Exception e) {
            logger.error("Error fetching all item categories: {}", e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @GetMapping("/{categoryId}")
    public GenericResponse<ItemCategoryDto> getCategory(@PathVariable Long categoryId) {
        logger.info("GET /api/v1/item-category/{} called", categoryId);
        var res = new GenericResponse<ItemCategoryDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            var category = itemCategoryService.getCategoryDtoById(categoryId);
            res.data = category;
        } catch (Exception e) {
            logger.error("Error fetching item category with ID {}: {}", categoryId, e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @PostMapping("/create")
    public GenericResponse<ItemCategoryDto> createCategory(@RequestBody ItemCategoryDto dto) {
        logger.info("POST /api/v1/item-category/create called with request: {}", dto);
        var res = new GenericResponse<ItemCategoryDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            ItemCategory category = new ItemCategory();
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            
            var createdCategory = itemCategoryService.createCategory(category);
            res.data = ItemCategoryDto.fromEntity(createdCategory);
        } catch (Exception e) {
            logger.error("Error creating item category: {}", e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @PostMapping("/create-bulk")
    public GenericResponse<List<ItemCategoryDto>> createCategories(@RequestBody List<ItemCategoryDto> dtos) {
        logger.info("POST /api/v1/item-category/create-bulk called with {} categories", dtos.size());
        var res = new GenericResponse<List<ItemCategoryDto>>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            List<ItemCategory> categories = dtos.stream()
                    .map(dto -> {
                        ItemCategory category = new ItemCategory();
                        category.setName(dto.getName());
                        category.setDescription(dto.getDescription());
                        return category;
                    })
                    .collect(Collectors.toList());
            
            var createdCategories = itemCategoryService.createCategories(categories);
            res.data = createdCategories.stream()
                    .map(ItemCategoryDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error creating item categories: {}", e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @PutMapping("/update/{categoryId}")
    public GenericResponse<ItemCategoryDto> updateCategory(@PathVariable Long categoryId, @RequestBody ItemCategoryDto dto) {
        logger.info("PUT /api/v1/item-category/update/{} called with request: {}", categoryId, dto);
        var res = new GenericResponse<ItemCategoryDto>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            ItemCategory category = new ItemCategory();
            category.setId(categoryId);
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            
            var updatedCategory = itemCategoryService.updateCategory(category);
            res.data = ItemCategoryDto.fromEntity(updatedCategory);
        } catch (Exception e) {
            logger.error("Error updating item category with ID {}: {}", categoryId, e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }

    @DeleteMapping("/delete/{categoryId}")
    public GenericResponse<String> deleteCategory(@PathVariable Long categoryId) {
        logger.info("DELETE /api/v1/item-category/delete/{} called", categoryId);
        var res = new GenericResponse<String>();
        ApiUser apiUser = ApiUserUtil.getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            res.message = "Api User is null";
            res.status = -1;
            return res;
        }

        try {
            itemCategoryService.deleteCategory(categoryId);
            res.data = "Category deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting item category with ID {}: {}", categoryId, e.getMessage());
            res.message = e.getMessage();
            res.status = -1;
        }
        return res;
    }
} 