package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.CardStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateCardRequest {

    @Size(max = 100, message = "The cardholder's name must be less than 100 characters long.")
    private String cardHolder;

    private CardStatus status;

    @DecimalMin(value = "0.00", inclusive = true, message = "The daily withdrawal limit must be non-negative")
    @Digits(integer=17, fraction=2, message = "The limit format is invalid (max. 17 integers, 2 fractional digits")
    private BigDecimal dailyWithdrawalLimit;

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }
}