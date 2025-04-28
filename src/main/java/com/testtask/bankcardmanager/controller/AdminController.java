package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.CreateUserRequest;
import com.testtask.bankcardmanager.dto.request.GetUsersRequest;
import com.testtask.bankcardmanager.dto.request.UpdateUserStatusRequest;
import com.testtask.bankcardmanager.dto.response.ErrorResponse;
import com.testtask.bankcardmanager.dto.response.UserResponse;
import com.testtask.bankcardmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Admin Controller", description = "API для управления пользователями (только для Администраторов)")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Создать нового пользователя (ADMIN)", description = "Создает пользователя с ролью ROLE_USER. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные входные данные",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation Error", summary = "Ошибка валидации DTO", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:00:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Request body validation failed. Check 'validationErrors' for details.",
                                      "path": "/api/users",
                                      "validationErrors": {
                                        "email": ["Email cannot be blank"],
                                        "password": ["Password must be between 8 and 128 characters"]
                                      }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:01:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/users",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (недостаточно прав)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:02:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/users",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "409", description = "Конфликт (email уже существует)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Conflict", summary = "Email существует", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:03:00.123Z",
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Email already exists",
                                      "path": "/api/users",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Получить пользователя по ID (ADMIN)", description = "Возвращает информацию о пользователе по его ID. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:04:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/users/1",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:05:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/users/1",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", summary = "User не найден", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:06:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with id: 999",
                                      "path": "/api/users/999",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID пользователя для поиска", required = true) @PathVariable Long id) {
        UserResponse userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Получить список всех пользователей (ADMIN)", description = "Возвращает пагинированный список пользователей с возможностью фильтрации. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей получен"),
            @ApiResponse(responseCode = "400", description = "Невалидные параметры запроса (фильтры, пагинация)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad Params", summary = "Неверный формат email", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:07:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Request parameter/path variable validation failed. Check 'validationErrors' for details.",
                                      "path": "/api/users",
                                      "validationErrors": {
                                        "email": ["Email should be valid if provided"]
                                       }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:08:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/users",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:09:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/users",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject @Valid GetUsersRequest getUsersRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UserResponse> userPage = userService.getAllUsers(getUsersRequest, pageable);
        return ResponseEntity.ok(userPage);
    }

    @Operation(summary = "Обновить статус блокировки пользователя (ADMIN)", description = "Блокирует или разблокирует аккаунт пользователя. Требуется роль ROLE_ADMIN. Администратор не может заблокировать сам себя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус пользователя обновлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные входные данные или попытка заблокировать себя",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Lock Self", summary = "Попытка блокировки себя", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:10:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Administrator cannot lock their own account.",
                                      "path": "/api/users/1/status",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:11:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/users/2/status",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:12:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/users/2/status",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Not Found", summary = "User не найден", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:13:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with ID: 999",
                                      "path": "/api/users/999/status",
                                      "validationErrors": null
                                    }""")))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "ID пользователя для обновления статуса", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse updatedUser = userService.updateUserStatus(id, request.getLocked());
        return ResponseEntity.ok(updatedUser);
    }
}