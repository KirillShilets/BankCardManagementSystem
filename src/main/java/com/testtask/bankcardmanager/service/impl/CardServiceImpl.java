package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.*;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.exception.CardOperationException;
import com.testtask.bankcardmanager.exception.DailyLimitExceededException;
import com.testtask.bankcardmanager.exception.InsufficientFundsException;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import com.testtask.bankcardmanager.model.enums.TransactionStatus;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.CardService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;
    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository, TransactionRepository transactionRepository, Clock clock) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CardResponse createCard(CreateCardRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolder(request.getCardHolder());
        card.setStatus(request.getStatus());
        card.setBalance(request.getBalance());
        card.setDailyWithdrawalLimit(BigDecimal.ZERO);

        try {
            YearMonth expiry = YearMonth.parse(request.getExpiryDate(), EXPIRY_DATE_FORMATTER);
            if (expiry.isBefore(YearMonth.now(this.clock))) {
                throw new ValidationException("Expiry date cannot be in the past");
            }
            card.setExpiryDate(expiry);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid expiry date format. Expected YYYY-MM.");
        } catch (ValidationException e) {
            throw e;
        }

        Card savedCard = cardRepository.save(card);
        return mapCardToCardResponse(savedCard);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #id)")
    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("The card was not found with the ID: " + id));
        return mapCardToCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<CardResponse> getAllCards(GetCardsRequest getCardsRequest, Pageable pageable) {

        Specification<Card> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (getCardsRequest.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), getCardsRequest.getUserId()));
            }
            if (getCardsRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), getCardsRequest.getStatus()));
            }
            if (getCardsRequest.getCardHolder() != null && !getCardsRequest.getCardHolder().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("cardHolder")), "%" + getCardsRequest.getCardHolder().toLowerCase() + "%"));
            }
            if (query.getOrderList().isEmpty() && pageable.getSort().isUnsorted()) {
                query.orderBy(criteriaBuilder.asc(root.get("id")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Card> cardPage = cardRepository.findAll(spec, pageable);

        return cardPage.map(this::mapCardToCardResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CardResponse updateCard(Long id, UpdateCardRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The card was not found with the ID: " + id));

        boolean updated = false;
        if (request.getCardHolder() != null) {
            card.setCardHolder(request.getCardHolder());
            updated = true;
        }
        if (request.getStatus() != null) {
            card.setStatus(request.getStatus());
            updated = true;
        }
        if (request.getDailyWithdrawalLimit() != null) {
            card.setDailyWithdrawalLimit(request.getDailyWithdrawalLimit());
            updated = true;
        }

        if (updated) {
            Card updatedCard = cardRepository.save(card);
            return mapCardToCardResponse(updatedCard);
        } else {;
            return mapCardToCardResponse(card);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A card with an ID " + id + " not found"));
        if (card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<CardResponse> getCurrentUserCards(Pageable pageable) {
        Long currentUserId = getCurrentUserId();

        Specification<Card> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), currentUserId);

        Page<Card> cardPage = cardRepository.findAll(spec, pageable);
        return cardPage.map(this::mapCardToCardResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #cardId)")
    public void blockCard(Long cardId) {
        Long currentUserId = getCurrentUserId();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("A card with an ID " + cardId + " not found"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardOperationException("You cannot block an expired card");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void transferFunds(TransferRequest request) {
        Long currentUserId = getCurrentUserId();

        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new CardOperationException("The source card and the destination card cannot be the same");
        }

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Source card with ID " + request.getFromCardId() + " not found"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient card with ID " + request.getToCardId() + " not found"));

        if (!fromCard.getUser().getId().equals(currentUserId) || !toCard.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("Both cards must belong to the current user.");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("The source card is inactive");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("The recipient's card is inactive");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on the source card");
        }

        BigDecimal amount = request.getAmount();
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        LocalDateTime transactionTime = LocalDateTime.now(clock);

        Transaction withdrawal = new Transaction(fromCard, amount.negate(), transactionTime, TransactionStatus.COMPLETED, transactionTime);
        Transaction deposit = new Transaction(toCard, amount, transactionTime, TransactionStatus.COMPLETED, transactionTime);

        transactionRepository.save(withdrawal);
        transactionRepository.save(deposit);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #cardId)")
    public TransactionResponse withdrawFunds(Long cardId, WithdrawalRequest request) {
        Long currentUserId = getCurrentUserId();
        BigDecimal amount = request.getAmount();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("A card with an ID " + cardId + " not found"));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("The operation is impossible: the card is inactive");
        }

        if (card.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on the card");
        }

        if (card.getDailyWithdrawalLimit() != null && amount.compareTo(card.getDailyWithdrawalLimit()) > 0) {
            throw new DailyLimitExceededException("The daily withdrawal limit has been exceeded");
        }

        card.setBalance(card.getBalance().subtract(amount));

        LocalDateTime transactionTime = LocalDateTime.now(clock);
        Transaction withdrawal = new Transaction(card, amount.negate(), transactionTime, TransactionStatus.COMPLETED, transactionTime);
        Transaction savedTransaction = transactionRepository.save(withdrawal);

        return mapTransactionToTransactionDto(savedTransaction);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("There is no authenticated user");
        }
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + userEmail + " not found"));
        return user.getId();
    }

    private TransactionResponse mapTransactionToTransactionDto(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCard() != null ? transaction.getCard().getId() : null,
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

    private CardResponse mapCardToCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardHolder(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getUser() != null ? card.getUser().getId() : null,
                card.getDailyWithdrawalLimit()
        );
    }

    private static String maskCardNumber(String number) {
        if (number == null || number.length() < 10) {
            return "******";
        }
        return number.substring(0, 6) + "******" + number.substring(number.length() - 4);
    }
}
