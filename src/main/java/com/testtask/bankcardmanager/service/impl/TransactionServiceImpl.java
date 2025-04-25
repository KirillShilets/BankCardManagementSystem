package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.exception.ResourceNotFoundException;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.repository.CardRepository;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @transactionSecurityService.isOwner(authentication, #id)")
    public TransactionResponse getTransactionById(Long id) {
        logger.debug("Attempting to find transaction by ID: {}", id);
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
