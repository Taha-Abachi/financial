package com.pars.financial.dto;

import com.pars.financial.entity.User;

public class SimpleUserDto {
    private Long id;
    private String name;

    public SimpleUserDto() {}

    public SimpleUserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SimpleUserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new SimpleUserDto(user.getId(), user.getName());
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
} 