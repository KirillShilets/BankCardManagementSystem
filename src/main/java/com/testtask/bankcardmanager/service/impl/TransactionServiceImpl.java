package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.model.User;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.repository.UserRepository;
import com.testtask.bankcardmanager.service.TransactionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, CardRepository cardRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @transactionSecurityService.isOwner(authentication, #id)")
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        return mapTransactionToTransactionDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #cardId)")
    public List<TransactionResponse> getAllTransactionsByCardId(Long cardId) {
        cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("The card with id: " + cardId + " not found."));
        List<Transaction> transactions = transactionRepository.findByCardId(cardId);

        return transactions.stream()
                .map(this::mapTransactionToTransactionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TransactionResponse> getTransactionsForCurrentUser() {
        Long currentUserId = getCurrentUserId();

        List<Card> userCards = cardRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), currentUserId));

        if (userCards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> cardIds = userCards.stream().map(Card::getId).collect(Collectors.toList());

        List<Transaction> transactions = transactionRepository.findAll((root, query, criteriaBuilder) ->
                root.get("card").get("id").in(cardIds)
        );
        transactions.sort((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()));


        return transactions.stream()
                .map(this::mapTransactionToTransactionDto)
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
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
}
