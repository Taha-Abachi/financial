package com.pars.financial.mapper;

import com.pars.financial.dto.UserInfoDto;
import com.pars.financial.entity.User;
import com.pars.financial.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Converts User entity to UserInfoDto with non-confidential data only
     * Excludes: password, nationalCode, apiKey
     */
    public UserInfoDto toUserInfoDto(User user) {
        if (user == null) {
            return null;
        }

        UserInfoDto dto = new UserInfoDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobilePhoneNumber(user.getMobilePhoneNumber());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Map role information
        if (user.getRole() != null) {
            dto.setRole(toUserRoleInfoDto(user.getRole()));
        }

        return dto;
    }

    /**
     * Converts UserRole entity to UserRoleInfoDto
     */
    private UserInfoDto.UserRoleInfoDto toUserRoleInfoDto(UserRole role) {
        if (role == null) {
            return null;
        }

        return new UserInfoDto.UserRoleInfoDto(
            role.getId(),
            role.getName(),
            role.getDescription()
        );
    }
}
