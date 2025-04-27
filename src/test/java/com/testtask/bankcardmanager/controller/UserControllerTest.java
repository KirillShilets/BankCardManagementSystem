package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.TransferRequest;
import com.testtask.bankcardmanager.dto.request.WithdrawalRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.exception.CardOperationException;
import com.testtask.bankcardmanager.exception.DailyLimitExceededException;
import com.testtask.bankcardmanager.exception.InsufficientFundsException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import com.testtask.bankcardmanager.model.enums.TransactionStatus;
import com.testtask.bankcardmanager.service.CardService;
import com.testtask.bankcardmanager.service.TransactionService;
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


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserController userController;

    private CardResponse cardResponse1;
    private CardResponse cardResponse2;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        YearMonth expiry = YearMonth.of(2025, 12);
        cardResponse1 = new CardResponse(1L, "111111******4444", "Test User", expiry, CardStatus.ACTIVE, BigDecimal.valueOf(1000), 1L, BigDecimal.valueOf(500));
        cardResponse2 = new CardResponse(2L, "555555******8888", "Test User", expiry, CardStatus.ACTIVE, BigDecimal.valueOf(500), 1L, BigDecimal.valueOf(200));
        transactionResponse = new TransactionResponse(100L, 1L, BigDecimal.valueOf(-50), LocalDateTime.now(), TransactionStatus.COMPLETED, LocalDateTime.now());
    }

    @Test
    @DisplayName("getCurrentUserCards - Успешное получение карт")
    void getCurrentUserCards_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        List<CardResponse> cardList = List.of(cardResponse1, cardResponse2);
        Page<CardResponse> cardPage = new PageImpl<>(cardList, pageable, cardList.size());

        when(cardService.getCurrentUserCards(any(Pageable.class))).thenReturn(cardPage);

        ResponseEntity<Page<CardResponse>> response = userController.getCurrentUserCards(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        verify(cardService).getCurrentUserCards(pageable);
    }

    @Test
    @DisplayName("getCurrentUserTransactions - Успешное получение транзакций")
    void getCurrentUserTransactions_Success() {
        List<TransactionResponse> transactionList = List.of(transactionResponse);
        when(transactionService.getTransactionsForCurrentUser()).thenReturn(transactionList);

        ResponseEntity<List<TransactionResponse>> response = userController.getCurrentUserTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(transactionResponse, response.getBody().get(0));
        verify(transactionService).getTransactionsForCurrentUser();
    }

    @Test
    @DisplayName("blockCard - Успешная блокировка карты")
    void blockCard_Success() {
        Long cardId = 1L;
        doNothing().when(cardService).blockCard(cardId);

        ResponseEntity<Void> response = userController.blockCard(cardId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(cardService).blockCard(cardId);
    }

    @Test
    @DisplayName("blockCard - Карта не найдена")
    void blockCard_NotFound() {
        Long cardId = 99L;
        doThrow(new ResourceNotFoundException("Card not found")).when(cardService).blockCard(cardId);

        assertThrows(ResourceNotFoundException.class, () -> userController.blockCard(cardId));
        verify(cardService).blockCard(cardId);
    }

    @Test
    @DisplayName("blockCard - Ошибка операции (например, карта уже заблокирована)")
    void blockCard_OperationError() {
        Long cardId = 1L;
        doThrow(new CardOperationException("Карта уже заблокирована")).when(cardService).blockCard(cardId);

        assertThrows(CardOperationException.class, () -> userController.blockCard(cardId));
        verify(cardService).blockCard(cardId);
    }


    @Test
    @DisplayName("transferFunds - Успешный перевод")
    void transferFunds_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        doNothing().when(cardService).transferFunds(any(TransferRequest.class));

        ResponseEntity<Void> response = userController.transferFunds(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(cardService).transferFunds(request);
    }

    @Test
    @DisplayName("transferFunds - Ошибка: Недостаточно средств")
    void transferFunds_InsufficientFunds() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(5000));

        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(cardService).transferFunds(any(TransferRequest.class));

        assertThrows(InsufficientFundsException.class, () -> userController.transferFunds(request));
        verify(cardService).transferFunds(request);
    }

    @Test
    @DisplayName("transferFunds - Ошибка: Карта не найдена")
    void transferFunds_CardNotFound() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(99L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        doThrow(new ResourceNotFoundException("Card not found"))
                .when(cardService).transferFunds(any(TransferRequest.class));

        assertThrows(ResourceNotFoundException.class, () -> userController.transferFunds(request));
        verify(cardService).transferFunds(request);
    }

    @Test
    @DisplayName("transferFunds - Ошибка операции (неактивная карта и т.д.)")
    void transferFunds_OperationError() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        doThrow(new CardOperationException("The source card is inactive"))
                .when(cardService).transferFunds(any(TransferRequest.class));

        assertThrows(CardOperationException.class, () -> userController.transferFunds(request));
        verify(cardService).transferFunds(request);
    }

    @Test
    @DisplayName("withdrawFunds - Успешное снятие")
    void withdrawFunds_Success() {
        Long cardId = 1L;
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(50));

        when(cardService.withdrawFunds(eq(cardId), any(WithdrawalRequest.class))).thenReturn(transactionResponse);

        ResponseEntity<TransactionResponse> response = userController.withdrawFunds(cardId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(transactionResponse, response.getBody());
        verify(cardService).withdrawFunds(cardId, request);
    }

    @Test
    @DisplayName("withdrawFunds - Ошибка: Недостаточно средств")
    void withdrawFunds_InsufficientFunds() {
        Long cardId = 1L;
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(2000));

        when(cardService.withdrawFunds(eq(cardId), any(WithdrawalRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds"));

        assertThrows(InsufficientFundsException.class, () -> userController.withdrawFunds(cardId, request));
        verify(cardService).withdrawFunds(cardId, request);
    }

    @Test
    @DisplayName("withdrawFunds - Ошибка: Превышен дневной лимит")
    void withdrawFunds_DailyLimitExceeded() {
        Long cardId = 1L;
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(600)); // Больше лимита testCard1

        when(cardService.withdrawFunds(eq(cardId), any(WithdrawalRequest.class)))
                .thenThrow(new DailyLimitExceededException("Daily limit exceeded"));

        assertThrows(DailyLimitExceededException.class, () -> userController.withdrawFunds(cardId, request));
        verify(cardService).withdrawFunds(cardId, request);
    }

    @Test
    @DisplayName("withdrawFunds - Ошибка: Карта не найдена")
    void withdrawFunds_CardNotFound() {
        Long cardId = 99L;
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(50));

        when(cardService.withdrawFunds(eq(cardId), any(WithdrawalRequest.class)))
                .thenThrow(new ResourceNotFoundException("Card not found"));

        assertThrows(ResourceNotFoundException.class, () -> userController.withdrawFunds(cardId, request));
        verify(cardService).withdrawFunds(cardId, request);
    }

    @Test
    @DisplayName("withdrawFunds - Ошибка операции (неактивная карта)")
    void withdrawFunds_OperationError() {
        Long cardId = 1L;
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(50));

        when(cardService.withdrawFunds(eq(cardId), any(WithdrawalRequest.class)))
                .thenThrow(new CardOperationException("The operation is impossible: the card is inactive"));

        assertThrows(CardOperationException.class, () -> userController.withdrawFunds(cardId, request));
        verify(cardService).withdrawFunds(cardId, request);
    }
}