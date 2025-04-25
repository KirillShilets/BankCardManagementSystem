package com.testtask.bankcardmanager.model;

import com.testtask.bankcardmanager.model.converter.CardNumberAttributeConverter;
import com.testtask.bankcardmanager.model.converter.YearMonthDateAttributeConverter;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_encrypt", nullable = false, unique = true, length = 511)
    @Convert(converter = CardNumberAttributeConverter.class)
    private String cardNumber;

    @Column(name = "card_holder", nullable = false, length = 100)
    private String cardHolder;

    @Column(name = "expiry_date", nullable = false, columnDefinition = "VARCHAR(8)")
    @Convert(converter = YearMonthDateAttributeConverter.class)
    private YearMonth expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @Column(name = "balance", nullable = false, precision = 17, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Transaction> historyOfTransactions = new ArrayList<>();

    @Column(name = "daily_withdrawal_limit", precision = 17, scale = 2, nullable = false)
    private BigDecimal dailyWithdrawalLimit;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Card() {}

    public Card(BigDecimal balance, CardStatus status, YearMonth expiryDate, String cardHolder, String cardNumber) {
        this.balance = balance;
        this.status = status;
        this.expiryDate = expiryDate;
        this.cardHolder = cardHolder;
        this.cardNumber = cardNumber;
    }

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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

    public List<Transaction> getHistoryOfTransactions() {
        return historyOfTransactions;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void addTransaction(Transaction transaction) {
        historyOfTransactions.add(transaction);
        transaction.setCard(this);
    }

    public void removeTransaction(Transaction transaction) {
        historyOfTransactions.remove(transaction);
        transaction.setCard(null);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return id != null && id.equals(card.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", cardNumber='" + maskCardNumber(cardNumber) + '\'' +
                ", cardHolder='" + cardHolder + '\'' +
                ", expiryDate=" + expiryDate +
                ", status=" + status +
                ", balance=" + balance +
                ", dailyWithdrawalLimit=" + dailyWithdrawalLimit +
                ", historyOfTransactionsCount=" + (historyOfTransactions != null ? historyOfTransactions.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    private String maskCardNumber(String number) {
        if (number == null || number.length() < 10) {
            return "******";
        }
        return number.substring(0, 6) + "******" + number.substring(number.length() - 4);
    }
}