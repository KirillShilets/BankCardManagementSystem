package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse getTransactionById(Long id);
    List<TransactionResponse> getAllTransactionsByCardId(Long cardId);
    List<TransactionResponse> getTransactionsForCurrentUser();
}
