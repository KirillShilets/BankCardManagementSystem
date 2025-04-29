package com.testtask.bankcardmanager.security.service;

import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("transactionSecurityService")
public class TransactionSecurityService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionSecurityService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication authentication, Long transactionId) {
        if (authentication == null || transactionId == null) {
            return false;
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            return false;
        }

        Optional<User> currentUserOpt = userRepository.findByEmail(userEmail);
        if (currentUserOpt.isEmpty()) {
            return false;
        }
        Long currentUserId = currentUserOpt.get().getId();

        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        Transaction transaction = transactionOpt.get();

        Card card = transaction.getCard();
        if (card == null) {
            return false;
        }

        User cardOwner = card.getUser();
        if (cardOwner == null) {
            return false;
        }
        Long cardOwnerId = cardOwner.getId();

        return currentUserId.equals(cardOwnerId);
    }
}