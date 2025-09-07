package com.pars.financial.dto;

import com.pars.financial.entity.ItemCategory;

public class ItemCategoryDto {
    private Long id;
    private String name;
    private String description;

    public ItemCategoryDto() {
    }

    public ItemCategoryDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static ItemCategoryDto fromEntity(ItemCategory itemCategory) {
        if (itemCategory == null) {
            return null;
        }
        
        return new ItemCategoryDto(
            itemCategory.getId(),
            itemCategory.getName(),
            itemCategory.getDescription()
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
} 