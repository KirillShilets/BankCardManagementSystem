package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.request.*;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponse createCard(CreateCardRequest request);
    CardResponse getCardById(Long id);
    Page<CardResponse> getAllCards(GetCardsRequest getCardsRequest, Pageable pageable);
    CardResponse updateCard(Long id, UpdateCardRequest request);
    void deleteCard(Long id);
    Page<CardResponse> getCurrentUserCards(Pageable pageable);
    void blockCard(Long cardId);
    void transferFunds(TransferRequest request);
    TransactionResponse withdrawFunds(Long cardId, WithdrawalRequest request);
}
