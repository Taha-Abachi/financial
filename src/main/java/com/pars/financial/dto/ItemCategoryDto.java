package com.pars.financial.dto;

import com.pars.financial.entity.ItemCategory;
import java.time.LocalDateTime;

public class ItemCategoryDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isDeleted;
    private Boolean isActive;
    private String deleteUser;
    private LocalDateTime deactiveDate;
    private LocalDateTime deleteDate;

    public ItemCategoryDto() {
    }

    public ItemCategoryDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDeleted = false;
        this.isActive = true;
    }

    public ItemCategoryDto(Long id, String name, String description, Boolean isDeleted, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDeleted = isDeleted;
        this.isActive = isActive;
    }

    public ItemCategoryDto(Long id, String name, String description, Boolean isDeleted, Boolean isActive, 
                          String deleteUser, LocalDateTime deactiveDate, LocalDateTime deleteDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDeleted = isDeleted;
        this.isActive = isActive;
        this.deleteUser = deleteUser;
        this.deactiveDate = deactiveDate;
        this.deleteDate = deleteDate;
    }

    public static ItemCategoryDto fromEntity(ItemCategory itemCategory) {
        if (itemCategory == null) {
            return null;
        }
        
        return new ItemCategoryDto(
            itemCategory.getId(),
            itemCategory.getName(),
            itemCategory.getDescription(),
            itemCategory.getIsDeleted(),
            itemCategory.getIsActive(),
            itemCategory.getDeleteUser(),
            itemCategory.getDeactiveDate(),
            itemCategory.getDeleteDate()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public LocalDateTime getDeactiveDate() {
        return deactiveDate;
    }

    public void setDeactiveDate(LocalDateTime deactiveDate) {
        this.deactiveDate = deactiveDate;
    }

    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(LocalDateTime deleteDate) {
        this.deleteDate = deleteDate;
    }
} 