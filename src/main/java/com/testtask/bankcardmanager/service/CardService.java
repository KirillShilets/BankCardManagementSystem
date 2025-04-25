package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.UpdateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface CardService {
    CardResponse createCard(CreateCardRequest request);
    CardResponse getCardById(Long id);
    Page<CardResponse> getAllCards(Specification<Card> spec, Pageable pageable);
    CardResponse updateCard(Long id, UpdateCardRequest request);
    void deleteCard(Long id);
}
