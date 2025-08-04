package com.pars.financial.dto;

import com.pars.financial.entity.UserRole;

public class UserRoleDto {
    private Long id;
    private String name;
    private String description;

    public UserRoleDto() {}

    public UserRoleDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static UserRoleDto fromEntity(UserRole role) {
        if (role == null) {
            return null;
        }
        return new UserRoleDto(role.getId(), role.getName(), role.getDescription());
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