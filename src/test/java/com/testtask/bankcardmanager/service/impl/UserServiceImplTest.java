package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateUserRequest;
import com.testtask.bankcardmanager.dto.request.GetUsersRequest;
import com.testtask.bankcardmanager.dto.response.UserResponse;
import com.testtask.bankcardmanager.exception.DuplicateEmailException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.Role;
import com.testtask.bankcardmanager.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "encodedPassword", Role.ROLE_USER);
        testUser.setId(1L);

        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("new@example.com");
        createUserRequest.setPassword("ValidPassword1!");

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            User savedUserCopy = new User();
            savedUserCopy.setId(userToSave.getId() != null ? userToSave.getId() : System.currentTimeMillis());
            savedUserCopy.setEmail(userToSave.getEmail());
            savedUserCopy.setRole(userToSave.getRole());
            savedUserCopy.setPassword(userToSave.getPassword());
            savedUserCopy.setAccountNonLocked(userToSave.isAccountNonLocked());
            savedUserCopy.setAccountNonExpired(userToSave.isAccountNonExpired());
            savedUserCopy.setCredentialsNonExpired(userToSave.isCredentialsNonExpired());
            savedUserCopy.setEnabled(userToSave.isEnabled());
            return savedUserCopy;
        });
    }

    @Test
    @DisplayName("createUser - Успешное создание пользователя")
    void createUser_Success() {
        when(userRepository.findByEmail(createUserRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");

        UserResponse createdUser = userService.createUser(createUserRequest);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals(createUserRequest.getEmail(), createdUser.getEmail());
        assertEquals(Role.ROLE_USER, createdUser.getRole());
        verify(userRepository).findByEmail(createUserRequest.getEmail());
        verify(passwordEncoder).encode(createUserRequest.getPassword());
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(createUserRequest.getEmail()) &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    @DisplayName("createUser - Ошибка при дублировании Email")
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.findByEmail(createUserRequest.getEmail())).thenReturn(Optional.of(testUser));

        assertThrows(DuplicateEmailException.class, () -> {
            userService.createUser(createUserRequest);
        });
        verify(userRepository).findByEmail(createUserRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById - Успешный поиск пользователя")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertEquals(testUser.getRole(), foundUser.getRole());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById - Пользователь не найден")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("getAllUsers - Успешное получение списка пользователей")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = List.of(testUser);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);

        Page<UserResponse> resultPage = userService.getAllUsers(new GetUsersRequest(), pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(testUser.getEmail(), resultPage.getContent().get(0).getEmail());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("updateUserStatus - Успешная блокировка/разблокировка")
    void updateUserStatus_Success() {
        Long userId = 2L;
        when(userRepository.findById(userId)).thenAnswer(invocation -> {
            User user = new User("update@example.com", "pass", Role.ROLE_USER);
            user.setId(userId);
            user.setAccountNonLocked(true);
            return Optional.of(user);
        }).thenAnswer(invocation -> {
            User user = new User("update@example.com", "pass", Role.ROLE_USER);
            user.setId(userId);
            user.setAccountNonLocked(false);
            return Optional.of(user);
        });


        UserResponse updatedUserLocked = userService.updateUserStatus(userId, true);
        assertNotNull(updatedUserLocked);
        assertEquals(userId, updatedUserLocked.getId());

        UserResponse updatedUserUnlocked = userService.updateUserStatus(userId, false);
        assertNotNull(updatedUserUnlocked);
        assertEquals(userId, updatedUserUnlocked.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        List<User> capturedUsers = userCaptor.getAllValues();

        assertEquals(2, capturedUsers.size(), "Should have captured two saved users");

        User firstCapturedUser = capturedUsers.get(0);
        assertFalse(firstCapturedUser.isAccountNonLocked(), "First captured user should have accountNonLocked=false");
        assertEquals(userId, firstCapturedUser.getId());

        User secondCapturedUser = capturedUsers.get(1);
        assertTrue(secondCapturedUser.isAccountNonLocked(), "Second captured user should have accountNonLocked=true");
        assertEquals(userId, secondCapturedUser.getId());
    }

    @Test
    @DisplayName("updateUserStatus - Пользователь не найден")
    void updateUserStatus_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserStatus(99L, true);
        });
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserStatus - Администратор пытается заблокировать себя")
    void updateUserStatus_AdminLocksSelf_ThrowsException() {
        User adminUser = new User("admin@example.com", "encodedPassword", Role.ROLE_ADMIN);
        adminUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(authentication.getName()).thenReturn("admin@example.com");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.updateUserStatus(1L, true);
        });
        assertEquals("Administrator cannot lock their own account.", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}