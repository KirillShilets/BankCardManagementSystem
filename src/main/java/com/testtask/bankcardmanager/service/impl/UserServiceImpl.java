package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateUserRequest;
import com.testtask.bankcardmanager.dto.request.GetUsersRequest;
import com.testtask.bankcardmanager.dto.response.UserResponse;
import com.testtask.bankcardmanager.exception.DuplicateEmailException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.Role;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.UserService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new DuplicateEmailException("Email already exists");
        });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);
        return mapUserToUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapUserToUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<UserResponse> getAllUsers(GetUsersRequest getUsersRequest, Pageable pageable) {

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getUsersRequest.getEmail() != null && !getUsersRequest.getEmail().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + getUsersRequest.getEmail().toLowerCase() + "%"));
            }
            if (getUsersRequest.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), getUsersRequest.getRole()));
            }
            if (getUsersRequest.getLocked() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountNonLocked"), !getUsersRequest.getLocked()));
            }

            if (query.getOrderList().isEmpty() && pageable.getSort().isUnsorted()) {
                query.orderBy(criteriaBuilder.asc(root.get("id")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userPage.map(this::mapUserToUserDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse updateUserStatus(Long id, Boolean locked) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && user.getEmail().equals(auth.getName()) && locked) {
            throw new ValidationException("Administrator cannot lock their own account.");
        }

        user.setAccountNonLocked(!locked);
        User updatedUser = userRepository.save(user);
        return mapUserToUserDto(updatedUser);
    }

    private UserResponse mapUserToUserDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
