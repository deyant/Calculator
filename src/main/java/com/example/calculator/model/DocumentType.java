package com.example.calculator.model;

import lombok.Getter;

public enum DocumentType {
    INVOICE(1),
    CREDIT_NOTE(2),
    DEBIT_NOTE(3);

    @Getter
    private int value;

    DocumentType(int value) {
        this.value = value;
    }

    public static DocumentType valueOf(int value) {
        for (DocumentType documentType : values()) {
            if (documentType.getValue() == value) {
                return documentType;
            }
        }
        return null;
    }
}
