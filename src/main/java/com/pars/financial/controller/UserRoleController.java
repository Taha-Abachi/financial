package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.UserRoleDto;
import com.pars.financial.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-roles")
public class UserRoleController {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping("/list")
    public GenericResponse<List<UserRoleDto>> getAllRoles() {
        logger.info("GET /api/v1/user-roles/list called");
        var response = new GenericResponse<List<UserRoleDto>>();
        try {
            var roles = userRoleService.getAllRoles();
            response.data = roles;
        } catch (Exception e) {
            logger.error("Error fetching user roles: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/{id}")
    public GenericResponse<UserRoleDto> getRoleById(@PathVariable Long id) {
        logger.info("GET /api/v1/user-roles/{} called", id);
        var response = new GenericResponse<UserRoleDto>();
        try {
            var role = userRoleService.getRoleById(id);
            response.data = role;
        } catch (Exception e) {
            logger.error("Error fetching user role with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/name/{name}")
    public GenericResponse<UserRoleDto> getRoleByName(@PathVariable String name) {
        logger.info("GET /api/v1/user-roles/name/{} called", name);
        var response = new GenericResponse<UserRoleDto>();
        try {
            var role = userRoleService.getRoleByName(name);
            response.data = role;
        } catch (Exception e) {
            logger.error("Error fetching user role with name {}: {}", name, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/create")
    public GenericResponse<UserRoleDto> createRole(@RequestBody UserRoleDto roleDto) {
        logger.info("POST /api/v1/user-roles/create called with request: {}", roleDto.getName());
        var response = new GenericResponse<UserRoleDto>();
        try {
            var createdRole = userRoleService.createRole(roleDto);
            response.data = createdRole;
        } catch (Exception e) {
            logger.error("Error creating user role: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PutMapping("/update/{id}")
    public GenericResponse<UserRoleDto> updateRole(@PathVariable Long id, @RequestBody UserRoleDto roleDto) {
        logger.info("PUT /api/v1/user-roles/update/{} called", id);
        var response = new GenericResponse<UserRoleDto>();
        try {
            var updatedRole = userRoleService.updateRole(id, roleDto);
            response.data = updatedRole;
        } catch (Exception e) {
            logger.error("Error updating user role with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<Void> deleteRole(@PathVariable Long id) {
        logger.info("DELETE /api/v1/user-roles/delete/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            userRoleService.deleteRole(id);
        } catch (Exception e) {
            logger.error("Error deleting user role with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/initialize-defaults")
    public GenericResponse<Void> initializeDefaultRoles() {
        logger.info("POST /api/v1/user-roles/initialize-defaults called");
        var response = new GenericResponse<Void>();
        try {
            userRoleService.initializeDefaultRoles();
            response.message = "Default roles initialized successfully";
        } catch (Exception e) {
            logger.error("Error initializing default roles: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }
} 