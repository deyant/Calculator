package com.example.calculator.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Holds pairs of a {@link java.util.Currency Currency} and exchange rate
 */
@Getter
public class ExchangeRate {

    private Currency currency;
    private BigDecimal rate;
    private boolean defaultCurrency;

    public ExchangeRate(Currency currency, BigDecimal rate) {
        this.currency = currency;
        this.rate = rate;
        defaultCurrency = BigDecimal.ONE.equals(rate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return currency.equals(that.currency) && rate.equals(that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency);
    }
}
