
package com.mx.mitienda.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ApiError {
    // Getters
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private Map<String, Object> details;

    public ApiError(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }

    public void addDetail(String key, Object value) {
        if (this.details == null) this.details = new LinkedHashMap<>();
        this.details.put(key, value);
    }

}

