package com.testtask.bankcardmanager.security.service;

import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("transactionSecurityService")
public class TransactionSecurityService {

    private static final Logger log = LoggerFactory.getLogger(TransactionSecurityService.class);

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionSecurityService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Authentication authentication, Long transactionId) {
        if (authentication == null || transactionId == null) {
            log.warn("Cannot check ownership: authentication or transactionId is null.");
            return false;
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            log.warn("Cannot check ownership: authentication principal name is null.");
            return false;
        }

        Optional<User> currentUserOpt = userRepository.findByEmail(userEmail);
        if (currentUserOpt.isEmpty()) {
            log.warn("Cannot check ownership: current user with email '{}' not found.", userEmail);
            return false;
        }
        Long currentUserId = currentUserOpt.get().getId();

        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            log.warn("Cannot check ownership: transaction with id '{}' not found.", transactionId);
            return false;
        }
        Transaction transaction = transactionOpt.get();

        Card card = transaction.getCard();
        if (card == null) {
            log.warn("Cannot check ownership: transaction with id '{}' is not associated with any card.", transactionId);
            return false;
        }

        User cardOwner = card.getUser();
        if (cardOwner == null) {
            log.warn("Cannot check ownership: card with id '{}' (from transaction id '{}') has no owner.", card.getId(), transactionId);
            return false;
        }
        Long cardOwnerId = cardOwner.getId();

        boolean isOwner = currentUserId.equals(cardOwnerId);
        log.debug("Ownership check for transactionId={}: currentUserEmail='{}' (userId={}), cardOwnerId={}, result={}",
                transactionId, userEmail, currentUserId, cardOwnerId, isOwner);
        return isOwner;
    }
}