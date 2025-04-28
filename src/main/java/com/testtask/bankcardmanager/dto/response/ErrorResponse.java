package com.testtask.bankcardmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponse {

    @Schema(description = "Время возникновения ошибки", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус код ошибки", accessMode = Schema.AccessMode.READ_ONLY)
    private int status;

    @Schema(description = "Краткое описание типа ошибки (HTTP статус)", accessMode = Schema.AccessMode.READ_ONLY)
    private String error;

    @Schema(description = "Сообщение об ошибке для пользователя", accessMode = Schema.AccessMode.READ_ONLY)
    private String message;

    @Schema(description = "Путь запроса, который вызвал ошибку", accessMode = Schema.AccessMode.READ_ONLY)
    private String path;

    @Schema(description = "Детали ошибок валидации (если применимо)", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private Map<String, List<String>> validationErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, List<String>> validationErrors) {
        this(status, error, message, path);
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<String>> validationErrors) {
        this.validationErrors = validationErrors;
    }
}