package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.CardStatus;

public class GetCardsRequest {

    private Long userId;

    private CardStatus status;

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