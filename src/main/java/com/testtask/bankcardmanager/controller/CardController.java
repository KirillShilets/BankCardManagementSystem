package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.GetCardsRequest;
import com.testtask.bankcardmanager.dto.request.UpdateCardRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Controller (Admin)", description = "API для управления картами (преимущественно Администратором)")
public class CardController {

    private final CardService cardService;
    private final TransactionService transactionService;

    public CardController(CardService cardService, TransactionService transactionService) {
        this.cardService = cardService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "Создать новую карту (ADMIN)", description = "Создает новую банковскую карту для указанного пользователя. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные входные данные",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad Request", summary = "Неверный формат номера карты", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:16:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Request body validation failed. Check 'validationErrors' for details.",
                                      "path": "/api/cards",
                                      "validationErrors": { "cardNumber": ["Card number must be 16 digits"] }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:17:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:18:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Пользователь (владелец карты) не найден",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "User Not Found", summary = "User не найден", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:19:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "User not found with ID: 999",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }""")))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardResponse createdCard = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @Operation(summary = "Получить карту по ID", description = "Возвращает информацию о карте по ее ID. Доступно Администратору или владельцу карты.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:20:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards/101",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не админ и не владелец)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Нет доступа к карте", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:21:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards/102",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:22:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "The card was not found with the ID: 999",
                                      "path": "/api/cards/999",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<CardResponse> getCardById(
            @Parameter(description = "ID карты для поиска", required = true) @PathVariable Long id) {
        CardResponse cardDto = cardService.getCardById(id);
        return ResponseEntity.ok(cardDto);
    }

    @Operation(summary = "Получить список всех карт (ADMIN)", description = "Возвращает пагинированный список карт с возможностью фильтрации. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список карт получен"),
            @ApiResponse(responseCode = "400", description = "Невалидные параметры запроса",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad Params", summary = "Неверный статус", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:23:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Parameter 'status' should be of type 'CardStatus'",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:24:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:25:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @ParameterObject @Valid GetCardsRequest getCardsRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<CardResponse> cardPage = cardService.getAllCards(getCardsRequest, pageable);
        return ResponseEntity.ok(cardPage);
    }

    @Operation(summary = "Обновить информацию о карте (ADMIN)", description = "Обновляет владельца, статус или лимит карты. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта обновлена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные входные данные",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad Data", summary = "Неверный лимит", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:26:00.123Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Request body validation failed. Check 'validationErrors' for details.",
                                      "path": "/api/cards/101",
                                      "validationErrors": { "dailyWithdrawalLimit": ["The limit format is invalid (max. 17 integers, 2 fractional digits"] }
                                    }"""))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:27:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards/101",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:28:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards/101",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:29:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "The card was not found with the ID: 999",
                                      "path": "/api/cards/999",
                                      "validationErrors": null
                                    }""")))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CardResponse> updateCard(
            @Parameter(description = "ID карты для обновления", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCardRequest request) {
        CardResponse updatedCard = cardService.updateCard(id, request);
        return ResponseEntity.ok(updatedCard);
    }

    @Operation(summary = "Удалить (заблокировать) карту (ADMIN)", description = "Устанавливает статус карты на BLOCKED. Фактического удаления не происходит. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно заблокирована (или уже была заблокирована)"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:30:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards/101",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:31:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards/101",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:32:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "A card with an ID 999 not found",
                                      "path": "/api/cards/999",
                                      "validationErrors": null
                                    }""")))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты для блокировки", required = true) @PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить историю транзакций карты (ADMIN)", description = "Возвращает список всех транзакций для указанной карты. Требуется роль ROLE_ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список транзакций получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Unauthorized", summary = "Нет JWT токена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:33:00.123Z",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Full authentication is required to access this resource",
                                      "path": "/api/cards/101/transactions",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Forbidden", summary = "Не роль ADMIN", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:34:00.123Z",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You do not have the required permissions to access this resource.",
                                      "path": "/api/cards/101/transactions",
                                      "validationErrors": null
                                    }"""))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Card Not Found", summary = "Карта не найдена", value =
                                    """
                                    {
                                      "timestamp": "2024-07-29T13:35:00.123Z",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "The card with id: 999 not found.",
                                      "path": "/api/cards/999/transactions",
                                      "validationErrors": null
                                    }""")))
    })
    @GetMapping("/{cardId}/transactions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(
            @Parameter(description = "ID карты для получения транзакций", required = true) @PathVariable Long cardId) {
        List<TransactionResponse> transactions = transactionService.getAllTransactionsByCardId(cardId);
        return ResponseEntity.ok(transactions);
    }
}