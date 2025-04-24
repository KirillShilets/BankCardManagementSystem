package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse getTransactionById(Long id);
}
