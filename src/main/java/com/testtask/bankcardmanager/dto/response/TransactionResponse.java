package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ответ с информацией о транзакции")
public class TransactionResponse {

    @Schema(description = "Уникальный идентификатор транзакции", example = "505", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID карты, к которой относится транзакция", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Long cardId;

    @Schema(description = "Сумма транзакции (отрицательная для списаний, положительная для пополнений)", example = "-50.00", type = "number", format = "double", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal amount;

    @Schema(description = "Дата и время проведения транзакции", example = "2024-07-28T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime transactionDate;

    @Schema(description = "Статус транзакции", example = "COMPLETED", accessMode = Schema.AccessMode.READ_ONLY)
    private TransactionStatus status;

    @Schema(description = "Дата и время создания записи о транзакции", example = "2024-07-28T10:15:31", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    public TransactionResponse(Long id, Long cardId, BigDecimal amount, LocalDateTime transactionDate, TransactionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.cardId = cardId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}