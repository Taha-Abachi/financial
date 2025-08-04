package com.pars.financial.service;

import com.pars.financial.dto.UserCreateRequest;
import com.pars.financial.dto.UserDto;
import com.pars.financial.dto.UserUpdateRequest;
import com.pars.financial.entity.User;
import com.pars.financial.entity.UserRole;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        logger.debug("Fetching user by id: {}", id);
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", id);
            throw new ValidationException("User not found", null, -121);
        }
        return UserDto.fromEntity(user.get());
    }

    public UserDto getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            logger.warn("User not found with username: {}", username);
            throw new ValidationException("User not found", null, -121);
        }
        return UserDto.fromEntity(user.get());
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

        // Create user with encrypted password
        var user = new User(
            request.getUsername(),
            request.getName(),
            passwordEncoder.encode(request.getPassword()),
            request.getMobilePhoneNumber(),
            request.getNationalCode(),
            role.get()
        );
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        var savedUser = userRepository.save(user);
        logger.info("Created user with id: {}", savedUser.getId());
        return UserDto.fromEntity(savedUser);
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
            user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        return UserDto.fromEntity(savedUser);
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
        return UserDto.fromEntity(savedUser);
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
        return UserDto.fromEntity(savedUser);
    }

    public List<UserDto> getUsersByRole(Long roleId) {
        logger.debug("Fetching users by role id: {}", roleId);
        return userRepository.findByRoleId(roleId).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<UserDto> getActiveUsers() {
        logger.debug("Fetching active users");
        return userRepository.findByIsActive(true).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
} 