package com.example.calculator.service;

import com.example.calculator.exception.CurrencyExchangeException;
import com.example.calculator.model.ExchangeRate;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static com.example.calculator.TestConstants.*;

public class CurrencyExchangeServiceTest {

    CurrencyExchangeService currencyExchangeService = new CurrencyExchangeService();

    @Test
    public void defaultToDefault() throws Exception {
        ExchangeRate defaultExchangeRate = new ExchangeRate(CURRENCY_EUR, BigDecimal.ONE);
        List<ExchangeRate> exchangeRateList = new LinkedList<>();
        exchangeRateList.add(defaultExchangeRate);

        Assert.assertEquals(new BigDecimal(100), currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_EUR, CURRENCY_EUR, new BigDecimal(100)));
    }

    @Test
    public void defaultToOthers() throws Exception {
        List<ExchangeRate> exchangeRateList = new LinkedList<>();
        ExchangeRate exchangeRateEur = new ExchangeRate(CURRENCY_EUR, BigDecimal.ONE);
        exchangeRateList.add(exchangeRateEur);

        ExchangeRate exchangeRateGbp = new ExchangeRate(CURRENCY_GBP, new BigDecimal("0.878"));
        exchangeRateList.add(exchangeRateGbp);

        ExchangeRate exchangeRateUsd = new ExchangeRate(CURRENCY_USD, new BigDecimal("0.987"));
        exchangeRateList.add(exchangeRateUsd);

        BigDecimal expectedGbp = new BigDecimal("87.80");
        Assert.assertEquals(expectedGbp, currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_EUR, CURRENCY_GBP, new BigDecimal(100)));

        BigDecimal expectedUsd = new BigDecimal("98.70");
        Assert.assertEquals(expectedUsd, currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_EUR, CURRENCY_USD, new BigDecimal(100)));
    }

    @Test
    public void nonDefault() throws Exception {
        List<ExchangeRate> exchangeRateList = new LinkedList<>();
        ExchangeRate exchangeRateEur = new ExchangeRate(CURRENCY_EUR, BigDecimal.ONE);
        exchangeRateList.add(exchangeRateEur);

        ExchangeRate exchangeRateGbp = new ExchangeRate(CURRENCY_GBP, new BigDecimal("0.878"));
        exchangeRateList.add(exchangeRateGbp);

        ExchangeRate exchangeRateUsd = new ExchangeRate(CURRENCY_USD, new BigDecimal("0.987"));
        exchangeRateList.add(exchangeRateUsd);

        BigDecimal expectedGbp = new BigDecimal("86.66");
        Assert.assertEquals(expectedGbp, currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_USD, CURRENCY_GBP, new BigDecimal(100)));
    }

    @Test(expected = CurrencyExchangeException.class)
    public void missingCurrency() throws Exception {
        List<ExchangeRate> exchangeRateList = new LinkedList<>();
        ExchangeRate exchangeRateEur = new ExchangeRate(CURRENCY_EUR, BigDecimal.ONE);
        exchangeRateList.add(exchangeRateEur);

        currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_USD, CURRENCY_BGN, new BigDecimal(100));
    }

    @Test(expected = CurrencyExchangeException.class)
    public void missingDefault() throws Exception {
        List<ExchangeRate> exchangeRateList = new LinkedList<>();

        ExchangeRate exchangeRateEur = new ExchangeRate(CURRENCY_EUR, new BigDecimal("2.3"));
        exchangeRateList.add(exchangeRateEur);

        ExchangeRate exchangeRateUsd = new ExchangeRate(CURRENCY_USD, new BigDecimal("1.2"));
        exchangeRateList.add(exchangeRateUsd);

        currencyExchangeService.convertAmount(exchangeRateList, CURRENCY_EUR, CURRENCY_USD, new BigDecimal(100));
    }
}
