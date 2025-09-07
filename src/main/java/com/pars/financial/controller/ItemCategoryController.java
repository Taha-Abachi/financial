package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.ItemCategoryDto;
import com.pars.financial.entity.User;
import com.pars.financial.service.ItemCategoryService;
import com.pars.financial.utils.ApiUserUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.pars.financial.entity.ItemCategory;

@RestController
@RequestMapping("/api/v1/item-category")
public class ItemCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryController.class);

    private final ItemCategoryService itemCategoryService;

    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @PostMapping("/create")
    public GenericResponse<ItemCategoryDto> createItemCategory(@RequestBody ItemCategoryDto request) {
        logger.info("POST /api/v1/item-category/create called");
        var response = new GenericResponse<ItemCategoryDto>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            
            // Convert DTO to entity
            ItemCategory category = new ItemCategory();
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            
            var createdItemCategory = itemCategoryService.createCategory(category);
            response.data = ItemCategoryDto.fromEntity(createdItemCategory);
        } catch (Exception e) {
            logger.error("Error creating item category: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/create-bulk")
    public GenericResponse<List<ItemCategoryDto>> createBulkItemCategories(@RequestBody List<ItemCategoryDto> request) {
        logger.info("POST /api/v1/item-category/create-bulk called");
        var response = new GenericResponse<List<ItemCategoryDto>>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var entities = request.stream()
                .map(dto -> {
                    ItemCategory category = new ItemCategory();
                    category.setName(dto.getName());
                    category.setDescription(dto.getDescription());
                    return category;
                })
                .collect(Collectors.toList());
            var createdItemCategories = itemCategoryService.createCategories(entities);
            response.data = createdItemCategories.stream()
                .map(ItemCategoryDto::fromEntity)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error creating bulk item categories: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/list")
    public GenericResponse<List<ItemCategoryDto>> getAllItemCategories() {
        logger.info("GET /api/v1/item-category/list called");
        var response = new GenericResponse<List<ItemCategoryDto>>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var itemCategories = itemCategoryService.getAllCategoryDtos();
            response.data = itemCategories;
        } catch (Exception e) {
            logger.error("Error fetching item categories: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/{id}")
    public GenericResponse<ItemCategoryDto> getItemCategoryById(@PathVariable Long id) {
        logger.info("GET /api/v1/item-category/{} called", id);
        var response = new GenericResponse<ItemCategoryDto>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var itemCategory = itemCategoryService.getCategoryDtoById(id);
            response.data = itemCategory;
        } catch (Exception e) {
            logger.error("Error fetching item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PutMapping("/update/{id}")
    public GenericResponse<ItemCategoryDto> updateItemCategory(@PathVariable Long id, @RequestBody ItemCategoryDto request) {
        logger.info("PUT /api/v1/item-category/update/{} called", id);
        var response = new GenericResponse<ItemCategoryDto>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            ItemCategory category = new ItemCategory();
            category.setId(id);
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            var updatedItemCategory = itemCategoryService.updateCategory(category);
            response.data = ItemCategoryDto.fromEntity(updatedItemCategory);
        } catch (Exception e) {
            logger.error("Error updating item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<Void> deleteItemCategory(@PathVariable Long id) {
        logger.info("DELETE /api/v1/item-category/delete/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            itemCategoryService.deleteCategory(id, apiUser.getUsername());
        } catch (Exception e) {
            logger.error("Error deleting item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/deactivate/{id}")
    public GenericResponse<Void> deactivateItemCategory(@PathVariable Long id) {
        logger.info("POST /api/v1/item-category/deactivate/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            itemCategoryService.deactivateCategory(id, apiUser.getUsername());
        } catch (Exception e) {
            logger.error("Error deactivating item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/activate/{id}")
    public GenericResponse<Void> activateItemCategory(@PathVariable Long id) {
        logger.info("POST /api/v1/item-category/activate/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            itemCategoryService.activateCategory(id);
        } catch (Exception e) {
            logger.error("Error activating item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/inactive")
    public GenericResponse<List<ItemCategoryDto>> getInactiveItemCategories() {
        logger.info("GET /api/v1/item-category/inactive called");
        var response = new GenericResponse<List<ItemCategoryDto>>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var itemCategories = itemCategoryService.getAllInactiveCategoryDtos();
            response.data = itemCategories;
        } catch (Exception e) {
            logger.error("Error fetching inactive item categories: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/all-including-inactive")
    public GenericResponse<List<ItemCategoryDto>> getAllItemCategoriesIncludingInactive() {
        logger.info("GET /api/v1/item-category/all-including-inactive called");
        var response = new GenericResponse<List<ItemCategoryDto>>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var itemCategories = itemCategoryService.getAllCategoryDtosIncludingInactive();
            response.data = itemCategories;
        } catch (Exception e) {
            logger.error("Error fetching all item categories including inactive: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/deleted")
    public GenericResponse<List<ItemCategoryDto>> getDeletedItemCategories() {
        logger.info("GET /api/v1/item-category/deleted called");
        var response = new GenericResponse<List<ItemCategoryDto>>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            var itemCategories = itemCategoryService.getAllDeletedCategoryDtos();
            response.data = itemCategories;
        } catch (Exception e) {
            logger.error("Error fetching deleted item categories: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/restore/{id}")
    public GenericResponse<Void> restoreItemCategory(@PathVariable Long id) {
        logger.info("POST /api/v1/item-category/restore/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            User apiUser = ApiUserUtil.getApiUserOrSetError(response, logger, "API user not found");
            if (apiUser == null) {
                return response;
            }
            itemCategoryService.restoreCategory(id);
        } catch (Exception e) {
            logger.error("Error restoring item category with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }
} 