package com.example.calculator.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;

/**
 * Default exception handler. Converts exceptions to HTTP responses.
 */
@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
