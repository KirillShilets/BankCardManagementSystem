package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.TransferRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.exception.CardOperationException;
import com.testtask.bankcardmanager.exception.InsufficientFundsException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import com.testtask.bankcardmanager.model.enums.Role;
import com.testtask.bankcardmanager.model.enums.TransactionStatus;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private Clock clock;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private Card testCard1;
    private Card testCard2;
    private final LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    private final YearMonth futureExpiry = YearMonth.of(2028, 12);
    private final String validCardNumber = "1111222233334444";

    @BeforeEach
    void setUp() {
        testUser = new User("user@example.com", "password", Role.ROLE_USER);
        testUser.setId(1L);

        testCard1 = new Card(BigDecimal.valueOf(1000), CardStatus.ACTIVE, futureExpiry, "Test User", validCardNumber);
        testCard1.setId(10L);
        testCard1.setUser(testUser);
        testCard1.setDailyWithdrawalLimit(BigDecimal.valueOf(500));

        testCard2 = new Card(BigDecimal.valueOf(500), CardStatus.ACTIVE, futureExpiry, "Test User", "5555666677778888");
        testCard2.setId(20L);
        testCard2.setUser(testUser);
        testCard2.setDailyWithdrawalLimit(BigDecimal.valueOf(200));

        Clock fixedClock = Clock.fixed(fixedTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());
        lenient().when(clock.withZone(any(ZoneId.class))).thenReturn(fixedClock);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn(testUser.getEmail());
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        lenient().when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("createCard - Успешное создание карты")
    void createCard_Success() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(testUser.getId());
        request.setCardNumber(validCardNumber);
        request.setCardHolder("New Card Holder");
        request.setExpiryDate(futureExpiry.toString());
        request.setStatus(CardStatus.ACTIVE);
        request.setBalance(BigDecimal.TEN);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card cardToSave = invocation.getArgument(0);
            cardToSave.setId(11L);
            return cardToSave;
        });

        CardResponse response = cardService.createCard(request);

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertEquals("New Card Holder", response.getCardHolder());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(0, BigDecimal.TEN.compareTo(response.getBalance()));
        assertEquals(futureExpiry, response.getExpiryDate());
        assertEquals(testUser.getId(), response.getUserId());
        assertTrue(response.getCardNumberMasked().startsWith(validCardNumber.substring(0, 6)));
        assertTrue(response.getCardNumberMasked().endsWith(validCardNumber.substring(validCardNumber.length() - 4)));
        assertTrue(response.getCardNumberMasked().contains("******"));
        assertEquals(BigDecimal.ZERO, response.getDailyWithdrawalLimit());

        verify(userRepository).findById(testUser.getId());
        verify(cardRepository).save(argThat(card ->
                card.getUser().equals(testUser) &&
                        card.getCardNumber().equals(validCardNumber) &&
                        card.getExpiryDate().equals(futureExpiry) &&
                        card.getDailyWithdrawalLimit().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    @DisplayName("createCard - Пользователь не найден")
    void createCard_UserNotFound_ThrowsException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(99L);
        request.setCardNumber(validCardNumber);
        request.setCardHolder("H");
        request.setExpiryDate(futureExpiry.toString());
        request.setStatus(CardStatus.ACTIVE);
        request.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cardService.createCard(request));
        verify(userRepository).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("createCard - Невалидная дата истечения (прошлая)")
    void createCard_InvalidExpiryDate_ThrowsException() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(testUser.getId());
        request.setCardNumber(validCardNumber);
        request.setCardHolder("Holder");
        request.setExpiryDate("2020-01");
        request.setStatus(CardStatus.ACTIVE);
        request.setBalance(BigDecimal.ZERO);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        assertThrows(jakarta.validation.ValidationException.class, () -> cardService.createCard(request));
        verify(userRepository).findById(testUser.getId());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("transferFunds - Успешный перевод между своими картами")
    void transferFunds_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(testCard1.getId());
        request.setToCardId(testCard2.getId());
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(testCard1.getId())).thenReturn(Optional.of(testCard1));
        when(cardRepository.findById(testCard2.getId())).thenReturn(Optional.of(testCard2));

        cardService.transferFunds(request);

        assertEquals(0, BigDecimal.valueOf(900).compareTo(testCard1.getBalance()));
        assertEquals(0, BigDecimal.valueOf(600).compareTo(testCard2.getBalance()));

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(transactionRepository).save(argThat(t ->
                t.getCard().equals(testCard1) &&
                        t.getAmount().compareTo(BigDecimal.valueOf(-100)) == 0 &&
                        t.getStatus() == TransactionStatus.COMPLETED &&
                        t.getTransactionDate().equals(fixedTime)
        ));
        verify(transactionRepository).save(argThat(t ->
                t.getCard().equals(testCard2) &&
                        t.getAmount().compareTo(BigDecimal.valueOf(100)) == 0 &&
                        t.getStatus() == TransactionStatus.COMPLETED &&
                        t.getTransactionDate().equals(fixedTime)
        ));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("transferFunds - Недостаточно средств")
    void transferFunds_InsufficientFunds_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(testCard1.getId());
        request.setToCardId(testCard2.getId());
        request.setAmount(BigDecimal.valueOf(2000));

        when(cardRepository.findById(testCard1.getId())).thenReturn(Optional.of(testCard1));
        when(cardRepository.findById(testCard2.getId())).thenReturn(Optional.of(testCard2));

        assertThrows(InsufficientFundsException.class, () -> cardService.transferFunds(request));
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(testCard1.getBalance()));
        assertEquals(0, BigDecimal.valueOf(500).compareTo(testCard2.getBalance()));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("transferFunds - Карта-источник неактивна")
    void transferFunds_InactiveSourceCard_ThrowsException() {
        testCard1.setStatus(CardStatus.BLOCKED);
        TransferRequest request = new TransferRequest();
        request.setFromCardId(testCard1.getId());
        request.setToCardId(testCard2.getId());
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(testCard1.getId())).thenReturn(Optional.of(testCard1));
        when(cardRepository.findById(testCard2.getId())).thenReturn(Optional.of(testCard2));

        assertThrows(CardOperationException.class, () -> cardService.transferFunds(request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("transferFunds - Перевод на ту же карту")
    void transferFunds_SameSourceAndTargetCard_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(testCard1.getId());
        request.setToCardId(testCard1.getId());
        request.setAmount(BigDecimal.valueOf(100));

        CardOperationException exception = assertThrows(CardOperationException.class, () -> cardService.transferFunds(request));
        assertEquals("The source card and the destination card cannot be the same", exception.getMessage());
        verify(cardRepository, never()).findById(anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("transferFunds - Карта-источник не найдена")
    void transferFunds_SourceCardNotFound_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(99L);
        request.setToCardId(testCard2.getId());
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.transferFunds(request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}