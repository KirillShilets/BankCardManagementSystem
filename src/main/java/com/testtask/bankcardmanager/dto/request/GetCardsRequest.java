package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.Parameter;

public class GetCardsRequest {

    @Parameter(description = "Фильтр по ID пользователя (владельца карты)")
    private Long userId;

    @Parameter(description = "Фильтр по статусу карты (ACTIVE, BLOCKED, EXPIRED)")
    private CardStatus status;

    @Parameter(description = "Фильтр по имени держателя карты (частичное совпадение, регистронезависимое)")
    private String cardHolder;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    @Override
    public String toString() {
        return "GetCardsRequest{" +
                "userId=" + userId +
                ", status=" + status +
                ", cardHolder='" + cardHolder + '\'' +
                '}';
    }
}