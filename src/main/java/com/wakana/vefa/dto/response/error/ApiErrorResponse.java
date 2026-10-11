package com.wakana.vefa.dto.response.error;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String error;
    private String message;
    private String path;

    public ApiErrorResponse(LocalDateTime timestamp, int status, String code, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.error = code;
        this.message = message;
        this.path = path;
    }

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
            boolean legacy) {
        this(timestamp, status, error, message, path);
    }
}
