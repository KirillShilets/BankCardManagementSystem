package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.CreateUserRequest;
import com.testtask.bankcardmanager.dto.request.GetUsersRequest;
import com.testtask.bankcardmanager.dto.request.UpdateUserStatusRequest;
import com.testtask.bankcardmanager.dto.response.UserResponse;
import com.testtask.bankcardmanager.exception.DuplicateEmailException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.enums.Role;
import com.testtask.bankcardmanager.service.UserService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private UserResponse userResponseAdmin;
    private UserResponse userResponseUser;

    @BeforeEach
    void setUp() {
        userResponseAdmin = new UserResponse(1L, "admin@example.com", Role.ROLE_ADMIN);
        userResponseUser = new UserResponse(2L, "user@example.com", Role.ROLE_USER);
    }

    @Test
    @DisplayName("createUser - Успешное создание пользователя")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("new@example.com");
        request.setPassword("ValidPassword1!");

        UserResponse createdResponse = new UserResponse(3L, "new@example.com", Role.ROLE_USER);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdResponse);

        ResponseEntity<UserResponse> response = adminController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getId());
        assertEquals("new@example.com", response.getBody().getEmail());
        verify(userService).createUser(request);
    }

    @Test
    @DisplayName("createUser - Дубликат Email")
    void createUser_DuplicateEmail_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@example.com");
        request.setPassword("ValidPassword1!");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateEmailException("Email already exists"));

        assertThrows(DuplicateEmailException.class, () -> adminController.createUser(request));
        verify(userService).createUser(request);
    }


    @Test
    @DisplayName("getUserById - Успешное получение пользователя")
    void getUserById_Success() {
        when(userService.getUserById(2L)).thenReturn(userResponseUser);

        ResponseEntity<UserResponse> response = adminController.getUserById(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userResponseUser, response.getBody());
        verify(userService).getUserById(2L);
    }

    @Test
    @DisplayName("getUserById - Пользователь не найден")
    void getUserById_NotFound() {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () -> adminController.getUserById(99L));
        verify(userService).getUserById(99L);
    }

    @Test
    @DisplayName("getAllUsers - Успешное получение списка пользователей")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        GetUsersRequest request = new GetUsersRequest();
        List<UserResponse> userList = List.of(userResponseUser);
        Page<UserResponse> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userService.getAllUsers(any(GetUsersRequest.class), any(Pageable.class))).thenReturn(userPage);

        ResponseEntity<Page<UserResponse>> response = adminController.getAllUsers(request, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(userResponseUser, response.getBody().getContent().get(0));
        verify(userService).getAllUsers(request, pageable);
    }

    @Test
    @DisplayName("updateUserStatus - Успешное обновление статуса")
    void updateUserStatus_Success() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setLocked(true);
        Long userId = 2L;

        UserResponse updatedResponse = new UserResponse(userId, "user@example.com", Role.ROLE_USER);
        when(userService.updateUserStatus(eq(userId), eq(true))).thenReturn(updatedResponse);

        ResponseEntity<UserResponse> response = adminController.updateUserStatus(userId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedResponse, response.getBody());
        verify(userService).updateUserStatus(userId, true);
    }

    @Test
    @DisplayName("updateUserStatus - Пользователь не найден")
    void updateUserStatus_NotFound() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setLocked(true);
        Long userId = 99L;

        when(userService.updateUserStatus(eq(userId), eq(true)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () -> adminController.updateUserStatus(userId, request));
        verify(userService).updateUserStatus(userId, true);
    }

    @Test
    @DisplayName("updateUserStatus - Админ блокирует себя (ошибка валидации из сервиса)")
    void updateUserStatus_AdminLocksSelf() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setLocked(true);
        Long adminId = 1L;

        when(userService.updateUserStatus(eq(adminId), eq(true)))
                .thenThrow(new ValidationException("Administrator cannot lock their own account."));

        assertThrows(ValidationException.class, () -> adminController.updateUserStatus(adminId, request));
        verify(userService).updateUserStatus(adminId, true);
    }
}