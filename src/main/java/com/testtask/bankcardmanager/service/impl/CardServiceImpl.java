package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.GetCardsRequest;
import com.testtask.bankcardmanager.dto.request.UpdateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.CardService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository, Clock clock) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CardResponse createCard(CreateCardRequest request) {
        logger.info("Attempting to create card for user ID: {}", request.getUserId());
        User user = userRepository.findById(request.getUserId()).orElseThrow(null);

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
            logger.error("Invalid expiry date format received: {}", request.getExpiryDate(), e);
            throw new IllegalArgumentException("Invalid expiry date format. Expected YYYY-MM.");
        } catch (ValidationException e) {
            logger.warn("Validation error during card creation: {}", e.getMessage());
            throw e;
        }

        Card savedCard = cardRepository.save(card);
        logger.info("Card created successfully with ID: {} for user ID: {}", savedCard.getId(), user.getId());
        return mapCardToCardResponse(savedCard);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #id)")
    public CardResponse getCardById(Long id) {
        logger.debug("Attempting to find card by ID: {}", id);
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("The card was not found with the ID: " + id));
        return mapCardToCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<CardResponse> getAllCards(GetCardsRequest getCardsRequest, Pageable pageable) {
        logger.info("Request to get a list of cards. Filters: {}. Pagination: {}",
                getCardsRequest, pageable);

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
        logger.debug("{} cards found on page {} (total items: {})",
                cardPage.getNumberOfElements(), pageable.getPageNumber(), cardPage.getTotalElements());

        return cardPage.map(this::mapCardToCardResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CardResponse updateCard(Long id, UpdateCardRequest request) {
        logger.info("Attempt to update the card from ID: {}", id);
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
                .orElseThrow(() -> new ResourceNotFoundException("The card was not found with the ID: " + id));
        if (card.getStatus() != CardStatus.BLOCKED) {
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        }
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
