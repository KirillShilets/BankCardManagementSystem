package com.testtask.bankcardmanager.service;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;

public interface CardService {
    CardResponse createCard(CreateCardRequest request);
    CardResponse getCardById(Long id);
}
