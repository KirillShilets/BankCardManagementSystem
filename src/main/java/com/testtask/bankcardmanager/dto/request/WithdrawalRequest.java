package com.testtask.bankcardmanager.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WithdrawalRequest {

    @NotNull(message = "The withdrawal amount cannot be empty")
    @DecimalMin(value = "0.01", message = "The withdrawal amount must be positive")
    @Digits(integer=17, fraction=2, message = "Incorrect sum format (max. 15 integers, 2 fractional digits)")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}