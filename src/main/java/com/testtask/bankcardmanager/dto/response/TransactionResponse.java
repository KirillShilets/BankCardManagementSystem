package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private Long cardId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public TransactionResponse(Long id, Long cardId, BigDecimal amount, LocalDateTime transactionDate, TransactionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.cardId = cardId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
