package com.testtask.bankcardmanager.dto.request;

import com.testtask.bankcardmanager.model.enums.CardStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateCardRequest {
    @NotNull
    private Long userId;

    @NotBlank(message = "Card number cannot be blank")
    @Pattern(regexp = "^\\d{16}$", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Card holder name cannot be blank")
    @Size(max = 100, message = "Card holder name must be less than 100 characters")
    private String cardHolder;

    @NotBlank(message = "Expiry date cannot be blank")
    @Pattern(regexp = "^(20\\d{2})-(0[1-9]|1[0-2])$", message = "Expiry date must be in YYYY-MM format")
    private String expiryDate;

    @NotNull(message = "Card status cannot be null")
    private CardStatus status;

    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Balance must be non-negative")
    @Digits(integer=15, fraction=2, message = "Balance format invalid (max 15 integer, 2 fractional digits)")
    private BigDecimal balance;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
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
}
