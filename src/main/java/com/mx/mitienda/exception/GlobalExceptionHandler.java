package com.mx.mitienda.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex){
       Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(Exception.class)
     public ResponseEntity<Map<String, Object>> handlerExcepcionesGenerales(Exception ex){
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("mensaje", ex.getMessage());
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIlegalArgumentException(IllegalArgumentException ex){
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Datos invalidos");
        response.put("mensaje", ex.getMessage());
        response.put("timestamp", new Date());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex){
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(),error.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Datos invalidos");
        response.put("errores", errors);
        response.put("timestamp", new Date());
        return ResponseEntity.badRequest().body(response);
    }
}
