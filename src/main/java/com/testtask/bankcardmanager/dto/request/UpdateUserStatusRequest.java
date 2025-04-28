package com.testtask.bankcardmanager.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на обновление статуса блокировки пользователя")
public class UpdateUserStatusRequest {

    @NotNull(message = "The lock status cannot be null")
    @Schema(description = "Статус блокировки: true - заблокировать, false - разблокировать", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean locked;

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}