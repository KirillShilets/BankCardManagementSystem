package com.testtask.bankcardmanager.security.service;

import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("cardSecurityService")
public class CardSecurityService {

    private static final Logger log = LoggerFactory.getLogger(CardSecurityService.class);

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardSecurityService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication authentication, Long cardId) {
        if (authentication == null || cardId == null) {
            return false;
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            log.warn("Cannot check ownership: authentication principal name is null.");
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            log.warn("Cannot check ownership: user with email '{}' not found.", userEmail);
            return false;
        }
        Long currentUserId = userOpt.get().getId();

        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            log.warn("Cannot check ownership: card with id '{}' not found.", cardId);
            return false;
        }

        Card card = cardOpt.get();
        boolean isOwner = card.getUser() != null && card.getUser().getId().equals(currentUserId);
        log.debug("Ownership check for cardId={}: userEmail='{}' (userId={}), cardOwnerId={}, result={}",
                cardId, userEmail, currentUserId, card.getUser() != null ? card.getUser().getId() : "null", isOwner);
        return isOwner;
    }
}