package com.example.calculator.exception;


import lombok.Getter;

@Getter
public class UnsupportedCurrencyException extends Exception {

    private String currencyCode;

    public UnsupportedCurrencyException(String currencyCode) {
        super("Unsupported ISO 4217 currency code: " + currencyCode);
        this.currencyCode = currencyCode;
    }
}
