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
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.repository.UserRoleRepository;
import com.pars.financial.repository.StoreRepository;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.Company;
import com.pars.financial.enums.UserRole;
import com.pars.financial.utils.ApiKeyEncryption;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final StoreRepository storeRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, 
                      StoreRepository storeRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder, ApiKeyEncryption apiKeyEncryption) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.storeRepository = storeRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        
        // Set the API key encryption in the User entity
        User.setApiKeyEncryption(apiKeyEncryption);
    }

    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAllWithRelationships().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        logger.debug("Fetching user by id: {}", id);
        var user = userRepository.findByIdWithRelationships(id);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
        }
        return convertToUserDto(user.get());
    }

    public UserDto getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.warn("User not found with username: {}", username);
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
        }
        return convertToUserDto(user.get());
    }

    public User getUserEntityByUsername(String username) {
        logger.debug("Fetching user entity by username: {}", username);
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.warn("User not found with username: {}", username);
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
        }
        return user.get();
    }

    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        logger.info("Creating new user: {}", request.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Username {} already exists", request.getUsername());
            throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "Username already exists");
        }

        if (userRepository.existsByMobilePhoneNumber(request.getMobilePhoneNumber())) {
            logger.warn("Mobile phone number {} already exists", request.getMobilePhoneNumber());
            throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "Mobile phone number already exists");
        }

        if (userRepository.existsByNationalCode(request.getNationalCode())) {
            logger.warn("National code {} already exists", request.getNationalCode());
            throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "National code already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email {} already exists", request.getEmail());
            throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "Email already exists");
        }

        // Validate role exists
        var role = userRoleRepository.findById(request.getRoleId());
        if (role.isEmpty()) {
            logger.warn("User role not found with id: {}", request.getRoleId());
            throw new ValidationException(ErrorCodes.USER_ROLE_NOT_FOUND);
        }

        // Validate store assignment for STORE_USER role
        Store store = null;
        if (UserRole.STORE_USER.name().equals(role.get().getName())) {
            if (request.getStoreId() == null) {
                logger.warn("Store ID is required for STORE_USER role");
                throw new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING, "Store ID is required for STORE_USER role");
            }
            
            var storeOptional = storeRepository.findById(request.getStoreId());
            if (storeOptional.isEmpty()) {
                logger.warn("Store not found with id: {}", request.getStoreId());
                throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
            }
            store = storeOptional.get();
        } else if (request.getStoreId() != null) {
            logger.warn("Store ID should not be provided for non-STORE_USER roles");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Store ID should only be provided for STORE_USER role");
        }

        // Validate company assignment for COMPANY_USER role
        Company company = null;
        if (UserRole.COMPANY_USER.name().equals(role.get().getName())) {
            if (request.getCompanyId() == null) {
                logger.warn("Company ID is required for COMPANY_USER role");
                throw new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING, "Company ID is required for COMPANY_USER role");
            }
            
            var companyOptional = companyRepository.findById(request.getCompanyId());
            if (companyOptional.isEmpty()) {
                logger.warn("Company not found with id: {}", request.getCompanyId());
                throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
            }
            company = companyOptional.get();
        } else if (request.getCompanyId() != null) {
            logger.warn("Company ID should not be provided for non-COMPANY_USER roles");
            throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Company ID should only be provided for COMPANY_USER role");
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
        
        // Set store for STORE_USER role
        if (store != null) {
            user.setStore(store);
            // Also set the company from the store for STORE_USER
            if (store.getCompany() != null) {
                user.setCompany(store.getCompany());
                logger.info("Assigned store {} and company {} to STORE_USER {}", store.getId(), store.getCompany().getId(), request.getUsername());
            } else {
                logger.warn("Store {} has no company assigned, STORE_USER {} will have null company", store.getId(), request.getUsername());
            }
        }

        // Set company for COMPANY_USER role
        if (company != null) {
            user.setCompany(company);
            logger.info("Assigned company {} to COMPANY_USER {}", company.getId(), request.getUsername());
        }
        
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
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
                throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "Mobile phone number already exists");
            }
            user.setMobilePhoneNumber(request.getMobilePhoneNumber());
        }

        if (request.getNationalCode() != null) {
            if (!request.getNationalCode().equals(user.getNationalCode()) && 
                userRepository.existsByNationalCode(request.getNationalCode())) {
                logger.warn("National code {} already exists", request.getNationalCode());
                throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "National code already exists");
            }
            user.setNationalCode(request.getNationalCode());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email {} already exists", request.getEmail());
                throw new ValidationException(ErrorCodes.USER_ALREADY_EXISTS, "Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRoleId() != null) {
            var role = userRoleRepository.findById(request.getRoleId());
            if (role.isEmpty()) {
                logger.warn("User role not found with id: {}", request.getRoleId());
                throw new ValidationException(ErrorCodes.USER_ROLE_NOT_FOUND);
            }
            
            // Validate role-specific requirements when changing role
            if (UserRole.STORE_USER.name().equals(role.get().getName()) && request.getStoreId() == null) {
                logger.warn("Store ID is required when changing role to STORE_USER");
                throw new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING, "Store ID is required when changing role to STORE_USER");
            }
            
            if (UserRole.COMPANY_USER.name().equals(role.get().getName()) && request.getCompanyId() == null) {
                logger.warn("Company ID is required when changing role to COMPANY_USER");
                throw new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING, "Company ID is required when changing role to COMPANY_USER");
            }
            
            // Clear assignments when changing to roles that don't need them
            if (!UserRole.STORE_USER.name().equals(role.get().getName())) {
                user.setStore(null);
                logger.info("Cleared store assignment for user {} when changing to role {}", user.getUsername(), role.get().getName());
            }
            
            if (!UserRole.COMPANY_USER.name().equals(role.get().getName())) {
                user.setCompany(null);
                logger.info("Cleared company assignment for user {} when changing to role {}", user.getUsername(), role.get().getName());
            }
            
            user.setRole(role.get());
            
            // If changing to STORE_USER role and storeId is provided, set the company from the store
            if (UserRole.STORE_USER.name().equals(role.get().getName()) && request.getStoreId() != null) {
                var storeOptional = storeRepository.findById(request.getStoreId());
                if (storeOptional.isPresent()) {
                    Store store = storeOptional.get();
                    if (store.getCompany() != null) {
                        user.setCompany(store.getCompany());
                        logger.info("Set company {} from store {} for user {} when changing to STORE_USER role", store.getCompany().getId(), store.getId(), user.getUsername());
                    } else {
                        logger.warn("Store {} has no company assigned, user {} will have null company when changing to STORE_USER role", store.getId(), user.getUsername());
                    }
                }
            }
        }

        // Handle store assignment updates
        if (request.getStoreId() != null) {
            if (UserRole.STORE_USER.name().equals(user.getRole().getName())) {
                var storeOptional = storeRepository.findById(request.getStoreId());
                if (storeOptional.isEmpty()) {
                    logger.warn("Store not found with id: {}", request.getStoreId());
                    throw new ValidationException(ErrorCodes.STORE_NOT_FOUND);
                }
                Store store = storeOptional.get();
                user.setStore(store);
                // Also set the company from the store for STORE_USER
                if (store.getCompany() != null) {
                    user.setCompany(store.getCompany());
                    logger.info("Updated store assignment for user {} to store {} and company {}", user.getUsername(), store.getId(), store.getCompany().getId());
                } else {
                    logger.warn("Store {} has no company assigned, user {} will have null company", store.getId(), user.getUsername());
                }
            } else {
                logger.warn("Store ID should only be provided for STORE_USER role");
                throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Store ID should only be provided for STORE_USER role");
            }
        }

        // Handle company assignment updates
        if (request.getCompanyId() != null) {
            if (UserRole.COMPANY_USER.name().equals(user.getRole().getName())) {
                var companyOptional = companyRepository.findById(request.getCompanyId());
                if (companyOptional.isEmpty()) {
                    logger.warn("Company not found with id: {}", request.getCompanyId());
                    throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
                }
                user.setCompany(companyOptional.get());
                logger.info("Updated company assignment for user {} to company {}", user.getUsername(), request.getCompanyId());
            } else {
                logger.warn("Company ID should only be provided for COMPANY_USER role");
                throw new ValidationException(ErrorCodes.INVALID_REQUEST, "Company ID should only be provided for COMPANY_USER role");
            }
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
        }

        var userEntity = user.get();
        
        // Check if user can use API keys
        if (!userEntity.canUseApiKey()) {
            logger.warn("User {} cannot use API keys", userEntity.getUsername());
            throw new ValidationException(ErrorCodes.INSUFFICIENT_PERMISSIONS, "User role does not support API key usage");
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
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
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
        return userRepository.findByRoleIdWithRelationships(roleId).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getActiveUsers() {
        logger.debug("Fetching active users");
        return userRepository.findByIsActiveWithRelationships(true).stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersWithApiKeyCapability() {
        logger.debug("Fetching users with API key capability");
        return userRepository.findAllWithRelationships().stream()
                .filter(User::canUseApiKey)
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersWithActiveApiKeys() {
        logger.debug("Fetching users with active API keys");
        return userRepository.findAllWithRelationships().stream()
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
            throw new ValidationException(ErrorCodes.API_KEY_INVALID);
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

        // Set store information if available
        if (user.getStore() != null) {
            userDto.setStoreId(user.getStore().getId());
            userDto.setStoreName(user.getStore().getStore_name());
        }

        // Set company information if available
        if (user.getCompany() != null) {
            userDto.setCompanyId(user.getCompany().getId());
            userDto.setCompanyName(user.getCompany().getName());
        }
        
        return userDto;
    }
} 