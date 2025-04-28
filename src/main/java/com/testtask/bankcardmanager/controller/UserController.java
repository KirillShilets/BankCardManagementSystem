package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.TransferRequest;
import com.testtask.bankcardmanager.dto.request.WithdrawalRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.ErrorResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.service.CardService;
import com.testtask.bankcardmanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Self-Service Controller", description = "API для действий текущего аутентифицированного пользователя со своими картами и транзакциями")
public class UserController {

    private final CardService cardService;
    private final TransactionService transactionService;

    public UserController(CardService cardService, TransactionService transactionService) {
        this.cardService = cardService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "Получить список карт текущего пользователя", description = "Возвращает пагинированный список карт, принадлежащих аутентифицированному пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:36:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/user/cards",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (например, аккаунт заблокирован)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Аккаунт заблокирован", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:37:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User account is locked",
                                      "path": "/api/user/cards",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardResponse>> getCurrentUserCards(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<CardResponse> cardPage = cardService.getCurrentUserCards(pageable);
        return ResponseEntity.ok(cardPage);
    }

    @Operation(summary = "Получить историю транзакций текущего пользователя", description = "Возвращает список всех транзакций по всем картам аутентифицированного пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список транзакций получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:38:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/user/transactions",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Аккаунт заблокирован", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:39:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User account is locked",
                                      "path": "/api/user/transactions",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponse>> getCurrentUserTransactions() {
        List<TransactionResponse> transactions = transactionService.getTransactionsForCurrentUser();
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Заблокировать свою карту", description = "Пользователь может заблокировать свою собственную активную карту.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "400", description = "Операция невозможна (карта уже заблокирована, истекла или не найдена)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Already Blocked", summary = "Карта уже заблокирована", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:40:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Карта уже заблокирована",
                                      "path": "/api/user/cards/101/block",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:41:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/user/cards/101/block",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка заблокировать чужую карту)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Попытка заблокировать чужую карту", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:42:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/user/cards/102/block",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта с указанным ID не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:43:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "A card with an ID 999 not found",
                                      "path": "/api/user/cards/999/block",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping("/cards/{id}/block")
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<Void> blockCard(
            @Parameter(description = "ID карты для блокировки", required = true) @PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевести средства между своими картами", description = "Позволяет пользователю перевести средства с одной своей активной карты на другую.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Операция невозможна (недостаточно средств, карты неактивны, одинаковые карты, неверная сумма)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Insufficient Funds", summary = "Недостаточно средств", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:44:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Insufficient funds on the source card",
                                      "path": "/api/user/cards/transfer",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:45:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/user/cards/transfer",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка использовать чужие карты)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Попытка использовать чужие карты", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:46:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Both cards must belong to the current user.",
                                      "path": "/api/user/cards/transfer",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Одна из карт не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта-получатель не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:47:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Recipient card with ID 999 not found",
                                      "path": "/api/user/cards/transfer",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping("/cards/transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> transferFunds(@Valid @RequestBody TransferRequest request) {
        cardService.transferFunds(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Снять средства со своей карты", description = "Позволяет пользователю снять средства со своей активной карты, если не превышен дневной лимит и достаточно баланса.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Средства успешно сняты",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Операция невозможна (недостаточно средств, карта неактивна, превышен лимит, неверная сумма)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Limit Exceeded", summary = "Превышен дневной лимит", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:48:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "The daily withdrawal limit has been exceeded",
                                      "path": "/api/user/cards/101/withdraw",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:49:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/user/cards/101/withdraw",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (попытка снять с чужой карты)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Попытка снять с чужой карты", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:50:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/user/cards/102/withdraw",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:51:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "A card with an ID 999 not found",
                                      "path": "/api/user/cards/999/withdraw",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping("/cards/{id}/withdraw")
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<TransactionResponse> withdrawFunds(
            @Parameter(description = "ID карты для снятия средств", required = true) @PathVariable Long id,
            @Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse transaction = cardService.withdrawFunds(id, request);
        return ResponseEntity.ok(transaction);
    }
}