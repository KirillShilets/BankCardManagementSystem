package com.testtask.bankcardmanager.dto.response;

import com.testtask.bankcardmanager.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Ответ с информацией о банковской карте")
public class CardResponse {

    @Schema(description = "Уникальный идентификатор карты", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Маскированный номер карты (первые 6 и последние 4 цифры)", example = "427688******6666", accessMode = Schema.AccessMode.READ_ONLY)
    private String cardNumberMasked;

    @Schema(description = "Имя держателя карты", example = "IVAN IVANOV", accessMode = Schema.AccessMode.READ_ONLY)
    private String cardHolder;

    @Schema(description = "Срок действия карты", example = "2028-12", type = "string", format = "yyyy-MM", accessMode = Schema.AccessMode.READ_ONLY)
    private YearMonth expiryDate;

    @Schema(description = "Текущий статус карты", example = "ACTIVE", accessMode = Schema.AccessMode.READ_ONLY)
    private CardStatus status;

    @Schema(description = "Текущий баланс карты", example = "950.50", type = "number", format = "double", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal balance;

    @Schema(description = "Дневной лимит снятия средств", example = "5000.00", type = "number", format = "double", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal dailyWithdrawalLimit;

    @Schema(description = "ID пользователя-владельца карты", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    public CardResponse(Long id, String cardNumberMasked, String cardHolder,
                        YearMonth expiryDate, CardStatus status, BigDecimal balance, Long userId,BigDecimal dailyWithdrawalLimit) {
        this.id = id;
        this.cardNumberMasked = cardNumberMasked;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
        this.userId = userId;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCardNumberMasked() { return cardNumberMasked; }
    public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }
    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }
    public YearMonth getExpiryDate() { return expiryDate; }
    public void setExpiryDate(YearMonth expiryDate) { this.expiryDate = expiryDate; }
    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getDailyWithdrawalLimit() { return dailyWithdrawalLimit; }
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) { this.dailyWithdrawalLimit = dailyWithdrawalLimit; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}