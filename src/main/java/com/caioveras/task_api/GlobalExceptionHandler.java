package com.caioveras.task_api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException validationException){
        Map<String, String> fieldMessages = new HashMap<>();

        for(FieldError error : validationException.getBindingResult().getFieldErrors()){
            fieldMessages.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(fieldMessages);
    }
}
