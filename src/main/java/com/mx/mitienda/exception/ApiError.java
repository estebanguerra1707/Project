/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mx.mitienda.exception.ApiError
 */
package com.mx.mitienda.exception;

import java.time.Instant;

public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String path;

    public static ApiError of(int status, String error, String path) {
        return new ApiError(Instant.now(), status, error, path);
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public int getStatus() {
        return this.status;
    }

    public String getError() {
        return this.error;
    }

    public String getPath() {
        return this.path;
    }

    public ApiError(Instant timestamp, int status, String error, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public ApiError() {
    }
}

