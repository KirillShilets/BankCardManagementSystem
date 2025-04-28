package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Запрос на обновление данных карты (только для Администратора)")
public class UpdateCardRequest {

    @Size(max = 100, message = "The cardholder's name must be less than 100 characters long.")
    @Schema(description = "Новое имя держателя карты (опционально)", example = "PETR PETROV", nullable = true)
    private String cardHolder;

    @Schema(description = "Новый статус карты (опционально)", example = "BLOCKED", nullable = true)
    private CardStatus status;

    @DecimalMin(value = "0.00", inclusive = true, message = "The daily withdrawal limit must be non-negative")
    @Digits(integer=17, fraction=2, message = "The limit format is invalid (max. 17 integers, 2 fractional digits")
    @Schema(description = "Новый дневной лимит снятия средств (опционально)", example = "10000.00", type = "number", format = "double", nullable = true)
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