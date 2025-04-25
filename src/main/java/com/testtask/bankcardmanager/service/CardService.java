package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.GetCardsRequest;
import com.testtask.bankcardmanager.dto.request.UpdateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponse createCard(CreateCardRequest request);
    CardResponse getCardById(Long id);
    Page<CardResponse> getAllCards(GetCardsRequest getCardsRequest, Pageable pageable);
    CardResponse updateCard(Long id, UpdateCardRequest request);
    void deleteCard(Long id);
}
