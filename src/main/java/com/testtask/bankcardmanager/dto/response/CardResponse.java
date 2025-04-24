package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

public class CardResponse {
    private Long id;
    private String cardNumberMasked;
    private String cardHolder;
    private YearMonth expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private Long userId;

    public CardResponse(Long id, String cardNumberMasked, String cardHolder, YearMonth expiryDate, CardStatus status, BigDecimal balance, Long userId) {
        this.id = id;
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public YearMonth getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(YearMonth expiryDate) {
        this.expiryDate = expiryDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
