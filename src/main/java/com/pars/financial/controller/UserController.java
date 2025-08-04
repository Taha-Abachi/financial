package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.UserCreateRequest;
import com.pars.financial.dto.UserDto;
import com.pars.financial.dto.UserUpdateRequest;
import com.pars.financial.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public GenericResponse<List<UserDto>> getAllUsers() {
        logger.info("GET /api/v1/users/list called");
        var response = new GenericResponse<List<UserDto>>();
        try {
            var users = userService.getAllUsers();
            response.data = users;
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/{id}")
    public GenericResponse<UserDto> getUserById(@PathVariable Long id) {
        logger.info("GET /api/v1/users/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var user = userService.getUserById(id);
            response.data = user;
        } catch (Exception e) {
            logger.error("Error fetching user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/username/{username}")
    public GenericResponse<UserDto> getUserByUsername(@PathVariable String username) {
        logger.info("GET /api/v1/users/username/{} called", username);
        var response = new GenericResponse<UserDto>();
        try {
            var user = userService.getUserByUsername(username);
            response.data = user;
        } catch (Exception e) {
            logger.error("Error fetching user with username {}: {}", username, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/create")
    public GenericResponse<UserDto> createUser(@RequestBody UserCreateRequest request) {
        logger.info("POST /api/v1/users/create called with username: {}", request.getUsername());
        var response = new GenericResponse<UserDto>();
        try {
            var createdUser = userService.createUser(request);
            response.data = createdUser;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PutMapping("/update/{id}")
    public GenericResponse<UserDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        logger.info("PUT /api/v1/users/update/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var updatedUser = userService.updateUser(id, request);
            response.data = updatedUser;
        } catch (Exception e) {
            logger.error("Error updating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/v1/users/delete/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            logger.error("Error deleting user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/activate/{id}")
    public GenericResponse<UserDto> activateUser(@PathVariable Long id) {
        logger.info("POST /api/v1/users/activate/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var activatedUser = userService.activateUser(id);
            response.data = activatedUser;
        } catch (Exception e) {
            logger.error("Error activating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/deactivate/{id}")
    public GenericResponse<UserDto> deactivateUser(@PathVariable Long id) {
        logger.info("POST /api/v1/users/deactivate/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var deactivatedUser = userService.deactivateUser(id);
            response.data = deactivatedUser;
        } catch (Exception e) {
            logger.error("Error deactivating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/role/{roleId}")
    public GenericResponse<List<UserDto>> getUsersByRole(@PathVariable Long roleId) {
        logger.info("GET /api/v1/users/role/{} called", roleId);
        var response = new GenericResponse<List<UserDto>>();
        try {
            var users = userService.getUsersByRole(roleId);
            response.data = users;
        } catch (Exception e) {
            logger.error("Error fetching users by role id {}: {}", roleId, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/active")
    public GenericResponse<List<UserDto>> getActiveUsers() {
        logger.info("GET /api/v1/users/active called");
        var response = new GenericResponse<List<UserDto>>();
        try {
            var users = userService.getActiveUsers();
            response.data = users;
        } catch (Exception e) {
            logger.error("Error fetching active users: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }
} 