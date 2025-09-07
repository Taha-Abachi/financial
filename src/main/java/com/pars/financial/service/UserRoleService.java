package com.pars.financial.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.UserRoleDto;
import com.pars.financial.entity.UserRole;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.UserRoleRepository;

@Service
public class UserRoleService {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleService.class);

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public List<UserRoleDto> getAllRoles() {
        logger.debug("Fetching all user roles");
        return userRoleRepository.findAll().stream()
                .map(this::convertToUserRoleDto)
                .collect(Collectors.toList());
    }

    public UserRoleDto getRoleById(Long id) {
        logger.debug("Fetching user role by id: {}", id);
        var role = userRoleRepository.findById(id);
        if (role.isEmpty()) {
            logger.warn("User role not found with id: {}", id);
            throw new ValidationException("User role not found", null, -119);
        }
        return convertToUserRoleDto(role.get());
    }

    public UserRoleDto getRoleByName(String name) {
        logger.debug("Fetching user role by name: {}", name);
        var role = userRoleRepository.findByName(name);
        if (role.isEmpty()) {
            logger.warn("User role not found with name: {}", name);
            throw new ValidationException("User role not found", null, -119);
        }
        return convertToUserRoleDto(role.get());
    }

    @Transactional
    public UserRoleDto createRole(UserRoleDto roleDto) {
        logger.info("Creating new user role: {}", roleDto.getName());
        
        if (userRoleRepository.existsByName(roleDto.getName())) {
            logger.warn("User role with name {} already exists", roleDto.getName());
            throw new ValidationException("User role with this name already exists", null, -120);
        }

        var role = new UserRole(roleDto.getName(), roleDto.getDescription());
        var savedRole = userRoleRepository.save(role);
        logger.info("Created user role with id: {}", savedRole.getId());
        return convertToUserRoleDto(savedRole);
    }

    @Transactional
    public UserRoleDto updateRole(Long id, UserRoleDto roleDto) {
        logger.info("Updating user role with id: {}", id);
        
        var existingRole = userRoleRepository.findById(id);
        if (existingRole.isEmpty()) {
            logger.warn("User role not found with id: {}", id);
            throw new ValidationException("User role not found", null, -119);
        }

        var role = existingRole.get();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());

        var savedRole = userRoleRepository.save(role);
        logger.info("Updated user role with id: {}", savedRole.getId());
        return convertToUserRoleDto(savedRole);
    }

    @Transactional
    public void deleteRole(Long id) {
        logger.info("Deleting user role with id: {}", id);
        
        if (!userRoleRepository.existsById(id)) {
            logger.warn("User role not found with id: {}", id);
            throw new ValidationException("User role not found", null, -119);
        }

        userRoleRepository.deleteById(id);
        logger.info("Deleted user role with id: {}", id);
    }

    public boolean existsByName(String name) {
        return userRoleRepository.existsByName(name);
    }

    @Transactional
    public void initializeDefaultRoles() {
        logger.info("Initializing default user roles");
        
        // Check and create SUPERADMIN role
        if (!existsByName("SUPERADMIN")) {
            createRole(new UserRoleDto(null, "SUPERADMIN", "Super Administrator with full system access"));
            logger.info("Created SUPERADMIN role");
        }
        
        // Check and create ADMIN role
        if (!existsByName("ADMIN")) {
            createRole(new UserRoleDto(null, "ADMIN", "Administrator with management access"));
            logger.info("Created ADMIN role");
        }
        
        // Check and create API_USER role
        if (!existsByName("API_USER")) {
            createRole(new UserRoleDto(null, "API_USER", "API User with limited access"));
            logger.info("Created API_USER role");
        }
        
        // Check and create USER role
        if (!existsByName("USER")) {
            createRole(new UserRoleDto(null, "USER", "Regular user with basic access"));
            logger.info("Created USER role");
        }
    }

    private UserRoleDto convertToUserRoleDto(UserRole userRole) {
        UserRoleDto roleDto = new UserRoleDto();
        roleDto.setId(userRole.getId());
        roleDto.setName(userRole.getName());
        roleDto.setDescription(userRole.getDescription());
        return roleDto;
    }
} 