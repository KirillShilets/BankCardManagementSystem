package com.testtask.bankcardmanager.model;

import com.testtask.bankcardmanager.model.enums.TransactionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Card card;

    @Column(name = "amount", nullable = false, precision = 17, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;



    public Transaction(Card card, BigDecimal amount, LocalDateTime transactionDate, TransactionStatus status, LocalDateTime createdAt) {
        this.card = card;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Transaction() {}

    public Long getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 31;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", cardId=" + (card != null ? card.getId() : "null") +
                ", amount=" + amount +
                ", transactionDate=" + transactionDate +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
