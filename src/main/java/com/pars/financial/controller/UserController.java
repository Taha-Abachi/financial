package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.UserCreateRequest;
import com.pars.financial.dto.UserDto;
import com.pars.financial.dto.UserUpdateRequest;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.pars.financial.dto.UserStatistics;

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
    public ResponseEntity<GenericResponse<UserDto>> getUserById(@PathVariable("id") Long id) {
        logger.info("GET /api/v1/users/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var user = userService.getUserById(id);
            if (user == null) {
                response.status = -1;
                response.message = "User not found with ID: " + id;
                return ResponseEntity.notFound().build();
            }
            response.data = user;
            response.message = "User retrieved successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error fetching user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error fetching user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<GenericResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        logger.info("GET /api/v1/users/username/{} called", username);
        var response = new GenericResponse<UserDto>();
        try {
            var user = userService.getUserByUsername(username);
            if (user == null) {
                response.status = -1;
                response.message = "User not found with username: " + username;
                return ResponseEntity.notFound().build();
            }
            response.data = user;
            response.message = "User retrieved successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error fetching user with username {}: {}", username, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error fetching user with username {}: {}", username, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<UserDto>> createUser(@RequestBody UserCreateRequest request) {
        logger.info("POST /api/v1/users/create called with username: {}", request.getUsername());
        var response = new GenericResponse<UserDto>();
        try {
            var createdUser = userService.createUser(request);
            response.data = createdUser;
            response.message = "User created successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating user: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericResponse<UserDto>> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateRequest request) {
        logger.info("PUT /api/v1/users/update/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var updatedUser = userService.updateUser(id, request);
            response.data = updatedUser;
            response.message = "User updated successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error updating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GenericResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        logger.info("DELETE /api/v1/users/delete/{} called", id);
        var response = new GenericResponse<Void>();
        try {
            userService.deleteUser(id);
            response.message = "User deleted successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error deleting user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error deleting user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<GenericResponse<UserDto>> activateUser(@PathVariable("id") Long id) {
        logger.info("POST /api/v1/users/activate/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var activatedUser = userService.activateUser(id);
            response.data = activatedUser;
            response.message = "User activated successfully";
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.warn("Validation error activating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error activating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<GenericResponse<UserDto>> deactivateUser(@PathVariable("id") Long id) {
        logger.info("POST /api/v1/users/deactivate/{} called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var deactivatedUser = userService.deactivateUser(id);
            response.data = deactivatedUser;
            response.message = "User deactivated successfully";
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.warn("Validation error deactivating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error deactivating user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/api-key/generate")
    public ResponseEntity<GenericResponse<UserDto>> generateApiKey(@PathVariable("id") Long id) {
        logger.info("POST /api/v1/users/{}/api-key/generate called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var userWithApiKey = userService.generateApiKey(id);
            response.data = userWithApiKey;
            response.message = "API key generated successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error generating API key for user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error generating API key for user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/api-key/revoke")
    public ResponseEntity<GenericResponse<UserDto>> revokeApiKey(@PathVariable("id") Long id) {
        logger.info("POST /api/v1/users/{}/api-key/revoke called", id);
        var response = new GenericResponse<UserDto>();
        try {
            var userWithoutApiKey = userService.revokeApiKey(id);
            response.data = userWithoutApiKey;
            response.message = "API key revoked successfully";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error revoking API key for user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error revoking API key for user with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

    @PostMapping("/initialize-super-admin")
    public GenericResponse<Void> initializeDefaultSuperAdmin() {
        logger.info("POST /api/v1/users/initialize-super-admin called");
        var response = new GenericResponse<Void>();
        try {
            userService.initializeDefaultSuperAdmin();
            response.message = "Default super admin user initialized successfully";
        } catch (Exception e) {
            logger.error("Error initializing default super admin: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/api-key-capable")
    public GenericResponse<List<UserDto>> getUsersWithApiKeyCapability() {
        logger.info("GET /api/v1/users/api-key-capable called");
        var response = new GenericResponse<List<UserDto>>();
        try {
            var users = userService.getUsersWithApiKeyCapability();
            response.data = users;
        } catch (Exception e) {
            logger.error("Error fetching users with API key capability: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/with-active-api-keys")
    public GenericResponse<List<UserDto>> getUsersWithActiveApiKeys() {
        logger.info("GET /api/v1/users/with-active-api-keys called");
        var response = new GenericResponse<List<UserDto>>();
        try {
            var users = userService.getUsersWithActiveApiKeys();
            response.data = users;
        } catch (Exception e) {
            logger.error("Error fetching users with active API keys: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/validate-api-key")
    public ResponseEntity<GenericResponse<UserDto>> validateApiKey(@RequestBody String apiKey) {
        logger.info("POST /api/v1/users/validate-api-key called");
        var response = new GenericResponse<UserDto>();
        try {
            var user = userService.getUserByApiKey(apiKey);
            if (user == null) {
                response.status = -1;
                response.message = "Invalid API key";
                return ResponseEntity.badRequest().body(response);
            }
            response.data = user;
            response.message = "API key is valid";
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error validating API key: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error validating API key: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/statistics")
    public GenericResponse<UserStatistics> getUserStatistics() {
        logger.info("GET /api/v1/users/statistics called");
        var response = new GenericResponse<UserStatistics>();
        try {
            var statistics = userService.getUserStatistics();
            response.data = statistics;
        } catch (Exception e) {
            logger.error("Error fetching user statistics: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }
} 