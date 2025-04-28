package com.testtask.bankcardmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на снятие средств с карты")
public class WithdrawalRequest {

    @NotNull(message = "The withdrawal amount cannot be empty")
    @DecimalMin(value = "0.01", message = "The withdrawal amount must be positive")
    @Digits(integer=17, fraction=2, message = "Incorrect sum format (max. 15 integers, 2 fractional digits)")
    @Schema(description = "Сумма снятия", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00", type = "number", format = "double")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}