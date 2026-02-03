package com.mx.mitienda.exception;

public class DuplicateProductException extends RuntimeException {
    private final String field;
    public DuplicateProductException(String field, String message) {
        super(message);
        this.field= field;
    }
    public String getField(){
        return field;
    }
}
