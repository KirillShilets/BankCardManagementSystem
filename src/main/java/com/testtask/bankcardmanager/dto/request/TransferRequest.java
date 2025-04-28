package com.testtask.bankcardmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод средств между картами")
public class TransferRequest {

    @NotNull(message = "The source card ID cannot be empty.")
    @Schema(description = "ID карты, с которой осуществляется перевод", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long fromCardId;

    @NotNull(message = "The recipient card's ID cannot be empty.")
    @Schema(description = "ID карты, на которую осуществляется перевод", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    private Long toCardId;

    @NotNull(message = "The transfer amount cannot be empty")
    @DecimalMin(value = "0.01", message = "The transfer amount must be positive")
    @Digits(integer=17, fraction=2, message = "Incorrect sum format (max. 17 integers, 2 fractional digits)")
    @Schema(description = "Сумма перевода", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.25", type = "number", format = "double")
    private BigDecimal amount;

    public Long getFromCardId() {
        return fromCardId;
    }

    public void setFromCardId(Long fromCardId) {
        this.fromCardId = fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public void setToCardId(Long toCardId) {
        this.toCardId = toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}