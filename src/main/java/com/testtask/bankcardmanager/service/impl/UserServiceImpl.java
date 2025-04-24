package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateUserRequest;
import com.testtask.bankcardmanager.dto.response.UserResponse;
import com.testtask.bankcardmanager.exception.DuplicateEmailException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.Role;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Attempting to create user with email: {}", request.getEmail());
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new DuplicateEmailException("Email already exists");
        });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return mapUserToUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        logger.debug("Attempting to find user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapUserToUserDto(user);
    }

    private UserResponse mapUserToUserDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
