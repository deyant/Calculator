package com.example.calculator.exception;

import lombok.Getter;

import java.util.Currency;

@Getter
public class CurrencyExchangeException extends Exception {

    private Currency fromCurrency;
    private Currency toCurrency;

    public CurrencyExchangeException(String message, Currency fromCurrency, Currency toCurrency) {
        super(message);
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }
}
