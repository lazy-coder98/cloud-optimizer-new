package com.cloud.optimizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", 400);

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        error.put("message", message);

        return error;
    }
}