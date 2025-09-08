package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pars.financial.dto.UserCreateRequest;
import com.pars.financial.dto.UserDto;
import com.pars.financial.dto.UserRoleDto;
import com.pars.financial.dto.UserStatistics;
import com.pars.financial.dto.UserUpdateRequest;
import com.pars.financial.entity.User;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.repository.UserRoleRepository;
import com.pars.financial.utils.ApiKeyEncryption;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, 
                      PasswordEncoder passwordEncoder, ApiKeyEncryption apiKeyEncryption) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        
        // Set the API key encryption in the User entity
        User.setApiKeyEncryption(apiKeyEncryption);
    }

    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        logger.debug("Fetching user by id: {}", id);
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }
        return convertToUserDto(user.get());
    }

    public UserDto getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.warn("User not found with username: {}", username);
            throw new ValidationException("User not found", null, -121);
        }
        return convertToUserDto(user.get());
    }

    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        logger.info("Creating new user: {}", request.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Username {} already exists", request.getUsername());
            throw new ValidationException("Username already exists", null, -122);
        }

        if (userRepository.existsByMobilePhoneNumber(request.getMobilePhoneNumber())) {
            logger.warn("Mobile phone number {} already exists", request.getMobilePhoneNumber());
            throw new ValidationException("Mobile phone number already exists", null, -123);
        }

        if (userRepository.existsByNationalCode(request.getNationalCode())) {
            logger.warn("National code {} already exists", request.getNationalCode());
            throw new ValidationException("National code already exists", null, -124);
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email {} already exists", request.getEmail());
            throw new ValidationException("Email already exists", null, -125);
        }

        // Validate role exists
        var role = userRoleRepository.findById(request.getRoleId());
        if (role.isEmpty()) {
            logger.warn("User role not found with id: {}", request.getRoleId());
            throw new ValidationException("User role not found", null, -119);
        }

        // Create user
        var user = new User(
            request.getUsername(),
            request.getName(),
            request.getPassword(),
            request.getMobilePhoneNumber(),
            request.getNationalCode(),
            role.get()
        );
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setApiKey(generateSecureApiKey());
        
        // Encode password before saving
        user.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
        
        var savedUser = userRepository.save(user);
        logger.info("Created user with id: {}", savedUser.getId());
        return convertToUserDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        logger.info("Updating user with id: {}", id);

        var existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }

        var user = existingUser.get();

        // Update fields if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPassword() != null) {
            user.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getMobilePhoneNumber() != null) {
            if (!request.getMobilePhoneNumber().equals(user.getMobilePhoneNumber()) && 
                userRepository.existsByMobilePhoneNumber(request.getMobilePhoneNumber())) {
                logger.warn("Mobile phone number {} already exists", request.getMobilePhoneNumber());
                throw new ValidationException("Mobile phone number already exists", null, -123);
            }
            user.setMobilePhoneNumber(request.getMobilePhoneNumber());
        }

        if (request.getNationalCode() != null) {
            if (!request.getNationalCode().equals(user.getNationalCode()) && 
                userRepository.existsByNationalCode(request.getNationalCode())) {
                logger.warn("National code {} already exists", request.getNationalCode());
                throw new ValidationException("National code already exists", null, -124);
            }
            user.setNationalCode(request.getNationalCode());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email {} already exists", request.getEmail());
                throw new ValidationException("Email already exists", null, -125);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRoleId() != null) {
            var role = userRoleRepository.findById(request.getRoleId());
            if (role.isEmpty()) {
                logger.warn("User role not found with id: {}", request.getRoleId());
                throw new ValidationException("User role not found", null, -119);
            }
            user.setRole(role.get());
        }

        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(user);
        logger.info("Updated user with id: {}", savedUser.getId());
        return convertToUserDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }

        userRepository.deleteById(id);
        logger.info("Deleted user with id: {}", id);
    }

    @Transactional
    public UserDto activateUser(Long id) {
        logger.info("Activating user with id: {}", id);

        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }

        var userEntity = user.get();
        userEntity.setActive(true);
        userEntity.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(userEntity);
        logger.info("Activated user with id: {}", savedUser.getId());
        return convertToUserDto(savedUser);
    }

    @Transactional
    public UserDto deactivateUser(Long id) {
        logger.info("Deactivating user with id: {}", id);

        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }

        var userEntity = user.get();
        userEntity.setActive(false);
        userEntity.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(userEntity);
        logger.info("Deactivated user with id: {}", savedUser.getId());
        return convertToUserDto(savedUser);
    }

    @Transactional
    public UserDto generateApiKey(Long userId) {
        logger.info("Generating API key for user with id: {}", userId);

        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", userId);
            throw new ValidationException("User not found", null, -121);
        }

        var userEntity = user.get();
        
        // Check if user can use API keys
        if (!userEntity.canUseApiKey()) {
            logger.warn("User {} cannot use API keys", userEntity.getUsername());
            throw new ValidationException("User role does not support API key usage", null, -126);
        }

        // Generate a new API key
        String apiKey = generateSecureApiKey();
        userEntity.setApiKey(apiKey);
        userEntity.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(userEntity);
        logger.info("Generated API key for user with id: {}", savedUser.getId());
        
        // Return user with the plain API key for display
        var userDto = convertToUserDto(savedUser);
        userDto.setApiKey(apiKey); // Set the plain API key
        return userDto;
    }

    @Transactional
    public UserDto revokeApiKey(Long userId) {
        logger.info("Revoking API key for user with id: {}", userId);

        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", userId);
            throw new ValidationException("User not found", null, -121);
        }

        var userEntity = user.get();
        userEntity.setApiKey(null);
        userEntity.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(userEntity);
        logger.info("Revoked API key for user with id: {}", savedUser.getId());
        return convertToUserDto(savedUser);
    }

    public List<UserDto> getUsersByRole(Long roleId) {
        logger.debug("Fetching users by role id: {}", roleId);
        return userRepository.findByRoleId(roleId).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getActiveUsers() {
        logger.debug("Fetching active users");
        return userRepository.findByIsActive(true).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersWithApiKeyCapability() {
        logger.debug("Fetching users with API key capability");
        return userRepository.findAll().stream()
                .filter(User::canUseApiKey)
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersWithActiveApiKeys() {
        logger.debug("Fetching users with active API keys");
        return userRepository.findAll().stream()
                .filter(User::hasApiKey)
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserDto getUserByApiKey(String apiKey) {
        logger.debug("Fetching user by API key");
        var user = userRepository.findByApiKey(apiKey);
        if (user.isEmpty()) {
            logger.warn("User not found with API key");
            throw new ValidationException("Invalid API key", null, -127);
        }
        return convertToUserDto(user.get());
    }

    public boolean validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByApiKey(apiKey);
    }

    public UserStatistics getUserStatistics() {
        logger.debug("Fetching user statistics");
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = userRepository.countByIsActive(false);
        long usersWithApiKeys = userRepository.findAll().stream()
                .filter(User::hasApiKey)
                .count();
        long apiKeyCapableUsers = userRepository.findAll().stream()
                .filter(User::canUseApiKey)
                .count();
        
        return new UserStatistics(totalUsers, activeUsers, inactiveUsers, usersWithApiKeys, apiKeyCapableUsers);
    }

    private String generateSecureApiKey() {
        // Generate a secure random API key
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void initializeDefaultSuperAdmin() {
        logger.info("Initializing default super admin user");
        
        // Check if any super admin exists
        var superAdminRole = userRoleRepository.findByName("SUPERADMIN");
        if (superAdminRole.isEmpty()) {
            logger.warn("SUPERADMIN role not found, cannot create default super admin");
            return;
        }
        
        // Check if any super admin user exists
        var existingSuperAdmin = userRepository.findByRoleId(superAdminRole.get().getId());
        if (!existingSuperAdmin.isEmpty()) {
            logger.info("Super admin user already exists, skipping initialization");
            return;
        }
        
        // Create default super admin user
        var superAdminUser = new User(
            "superadmin",
            "System Super Administrator",
            "admin123",
            "09123456789",
            "0000000000",
            superAdminRole.get()
        );
        superAdminUser.setEmail("admin@system.local");
        superAdminUser.setActive(true);
        superAdminUser.setCreatedAt(LocalDateTime.now());
        superAdminUser.setUpdatedAt(LocalDateTime.now());
        
        // Encode password before saving
        superAdminUser.setEncodedPassword(passwordEncoder.encode("admin123"));
        
        var savedUser = userRepository.save(superAdminUser);
        logger.info("Created default super admin user with id: {}", savedUser.getId());
    }

    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setMobilePhoneNumber(user.getMobilePhoneNumber());
        userDto.setNationalCode(user.getNationalCode());
        userDto.setActive(user.isActive());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());
        userDto.setApiKey(user.getApiKey() == null ? "" : user.getApiKey()); // Include API key in DTO
        
        if (user.getRole() != null) {
            UserRoleDto roleDto = new UserRoleDto();
            roleDto.setId(user.getRole().getId());
            roleDto.setName(user.getRole().getName());
            roleDto.setDescription(user.getRole().getDescription());
            userDto.setRole(roleDto);
        }
        
        return userDto;
    }
} 