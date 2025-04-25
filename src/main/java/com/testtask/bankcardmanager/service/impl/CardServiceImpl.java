package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.CardService;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class CardServiceImpl implements CardService {
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public CardServiceImpl(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CardResponse createCard(CreateCardRequest request) {
        logger.info("Attempting to create card for user ID: {}", request.getUserId());
        User user = userRepository.findById(request.getUserId()).orElseThrow(null);
        cardRepository.findByCardNumber(request.getCardNumber()).ifPresent(card -> {
            logger.warn("Potential duplicate card number detected before encryption (constraint will handle): {}", maskCardNumber(request.getCardNumber()));
        });

        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolder(request.getCardHolder());
        card.setStatus(request.getStatus());
        card.setBalance(request.getBalance());

        try {
            YearMonth expiry = YearMonth.parse(request.getExpiryDate(), EXPIRY_DATE_FORMATTER);
            if (expiry.isBefore(YearMonth.now())) throw new ValidationException("Expiry date cannot be in the past");
            card.setExpiryDate(expiry);
        } catch (DateTimeParseException e) {
            logger.error("Invalid expiry date format received: {}", request.getExpiryDate(), e);
            throw new IllegalArgumentException("Invalid expiry date format. Expected YYYY-MM.");
        }

        Card savedCard = cardRepository.save(card);
        logger.info("Card created successfully with ID: {} for user ID: {}", savedCard.getId(), user.getId());
        return mapCardToCardResponse(savedCard);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #id)")
    public CardResponse getCardById(Long id) {
        logger.debug("Attempting to find card by ID: {}", id);
        Card card = cardRepository.findById(id).orElseThrow();
        return mapCardToCardResponse(card);
    }

    private CardResponse mapCardToCardResponse(Card card) {
        return new CardResponse(
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardHolder(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getUser() != null ? card.getUser().getId() : null
        );
    }

    private static String maskCardNumber(String number) {
        if (number == null || number.length() < 10) {
            return "******";
        }
        return number.substring(0, 6) + "******" + number.substring(number.length() - 4);
    }
}
