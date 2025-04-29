package com.testtask.bankcardmanager.security.service;

import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("cardSecurityService")
public class CardSecurityService {

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
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return false;
        }
        Long currentUserId = userOpt.get().getId();

        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return false;
        }

        Card card = cardOpt.get();
        return card.getUser() != null && card.getUser().getId().equals(currentUserId);
    }
}