package com.testtask.bankcardmanager.service.impl;

import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.model.Transaction;
import com.testtask.bankcardmanager.repository.TransactionRepository;
import com.testtask.bankcardmanager.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        logger.debug("Attempting to find transaction by ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow();
        return mapTransactionToTransactionDto(transaction);
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
