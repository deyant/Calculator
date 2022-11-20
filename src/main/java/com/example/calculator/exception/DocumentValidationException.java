package com.example.calculator.exception;

import lombok.Getter;

public class DocumentValidationException extends Exception {

    @Getter
    private String documentNumber;

    public DocumentValidationException(String message, String documentNumber) {
        super("Validation error for document: [" + documentNumber + "]: " + message);
        this.documentNumber = documentNumber;
    }
}
