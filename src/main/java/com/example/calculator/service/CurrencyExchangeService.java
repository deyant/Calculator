package com.example.calculator.service;

import com.example.calculator.exception.CurrencyExchangeException;
import com.example.calculator.model.ExchangeRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;

import static com.example.calculator.Constants.DEFAULT_ROUNDING_MODE;

/**
 * Converts amounts from one currency to another using provided exchange rates.
 */
@Service
public class CurrencyExchangeService {

    /**
     * Convert amount from one currency to another using exchange rates.
     *
     * @param exchangeRates A list of exchange rates used for the conversion.
     * @param fromCurrency  Convert from currency
     * @param toCurrency    Convert to currency
     * @param amount        The amount to convert
     * @return Converted amount
     * @throws CurrencyExchangeException If the requested currencies are not present in the exchange rates list.
     */
    public BigDecimal convertAmount(final Collection<ExchangeRate> exchangeRates,
                                    final Currency fromCurrency,
                                    final Currency toCurrency,
                                    final BigDecimal amount)
            throws CurrencyExchangeException {

        ExchangeRate defaultExchangeRate = exchangeRates.stream()
                .filter(it -> it.isDefaultCurrency())
                .findFirst()
                .orElseThrow(() -> new CurrencyExchangeException("Unable to find default currency exchange rate",
                        fromCurrency, toCurrency));

        if (defaultExchangeRate.getCurrency().equals(fromCurrency) &&
                defaultExchangeRate.getCurrency().equals(toCurrency)) {
            return amount;
        }

        ExchangeRate fromExchangeRate = exchangeRates.stream()
                .filter(it -> it.getCurrency().equals(fromCurrency))
                .findFirst()
                .orElseThrow(() -> new CurrencyExchangeException(
                        String.format("Unable to find currency exchange rate for [%s]", fromCurrency),
                        fromCurrency, toCurrency));

        ExchangeRate toExchangeRate = exchangeRates.stream()
                .filter(it -> it.getCurrency().equals(toCurrency))
                .findFirst()
                .orElseThrow(() -> new CurrencyExchangeException(
                        String.format("Unable to find currency exchange rate for [%s]", toCurrency),
                        fromCurrency, toCurrency));

        BigDecimal amountInDefaultCurrency = amount.multiply(fromExchangeRate.getRate());
        BigDecimal amountInTargetCurrency = amountInDefaultCurrency
                .multiply(toExchangeRate.getRate())
                .setScale(toExchangeRate.getCurrency().getDefaultFractionDigits(), DEFAULT_ROUNDING_MODE);

        return amountInTargetCurrency;
    }
}
