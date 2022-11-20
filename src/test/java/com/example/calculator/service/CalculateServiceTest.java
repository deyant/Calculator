package com.example.calculator.service;

import com.example.calculator.exception.DocumentValidationException;
import com.example.calculator.model.Document;
import com.example.calculator.model.DocumentType;
import com.example.calculator.model.ExchangeRate;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static com.example.calculator.TestConstants.*;
import static org.junit.Assert.assertEquals;

public class CalculateServiceTest {

    private CalculateService calculateService;
    private CurrencyExchangeService currencyExchangeService;
    private List<ExchangeRate> exchangeRateList;

    @Before
    public void setup() {
        currencyExchangeService = new CurrencyExchangeService();
        calculateService = new CalculateService(currencyExchangeService);

        exchangeRateList = new LinkedList<>();
        ExchangeRate exchangeRateEur = new ExchangeRate(CURRENCY_EUR, BigDecimal.ONE);
        exchangeRateList.add(exchangeRateEur);

        ExchangeRate exchangeRateGbp = new ExchangeRate(CURRENCY_GBP, new BigDecimal(0.878));
        exchangeRateList.add(exchangeRateGbp);

        ExchangeRate exchangeRateUsd = new ExchangeRate(CURRENCY_USD, new BigDecimal(0.987));
        exchangeRateList.add(exchangeRateUsd);
    }

    @Test
    public void invoicesDefaultCurrency() throws Exception {
        List<Document> documents = new LinkedList<>();
        documents.add(Document.builder("1000", DocumentType.INVOICE).currency(CURRENCY_EUR).total(new BigDecimal(100.01)).build());
        documents.add(Document.builder("1001", DocumentType.INVOICE).currency(CURRENCY_EUR).total(new BigDecimal(100.01)).build());
        documents.add(Document.builder("1002", DocumentType.INVOICE).currency(CURRENCY_EUR).total(new BigDecimal(100.01)).build());

        BigDecimal totalSum = calculateService.getDocumentsTotalSum(documents, CURRENCY_EUR, exchangeRateList);
        BigDecimal expectedTotalSum = new BigDecimal("300.03");
        assertEquals(expectedTotalSum, totalSum);
    }

    @Test
    public void invoicesNonDefaultCurrency() throws Exception {
        List<Document> documents = new LinkedList<>();
        documents.add(Document.builder("1000", DocumentType.INVOICE).currency(CURRENCY_USD).total(new BigDecimal("100.01")).build());
        documents.add(Document.builder("1001", DocumentType.INVOICE).currency(CURRENCY_GBP).total(new BigDecimal("100.01")).build());
        documents.add(Document.builder("1002", DocumentType.INVOICE).currency(CURRENCY_EUR).total(new BigDecimal("100.01")).build());

        BigDecimal totalSum = calculateService.getDocumentsTotalSum(documents, CURRENCY_EUR, exchangeRateList);
        BigDecimal expectedTotalSum = new BigDecimal("286.53");
        assertEquals(expectedTotalSum, totalSum);
    }

    @Test
    public void invoicesWithParentDocument() throws Exception {
        List<Document> documents = new LinkedList<>();
        documents.add(Document.builder("1000", DocumentType.INVOICE).currency(CURRENCY_USD).total(new BigDecimal(100.01)).build());
        documents.add(Document.builder("1001", DocumentType.INVOICE).currency(CURRENCY_GBP).total(new BigDecimal(100.01)).build());
        documents.add(Document.builder("1002", DocumentType.INVOICE).currency(CURRENCY_EUR).total(new BigDecimal(100.01)).build());
        documents.add(Document.builder("1003", DocumentType.CREDIT_NOTE).currency(CURRENCY_USD).total(new BigDecimal(100.01)).parentDocumentNumber("1000").build());
        documents.add(Document.builder("1004", DocumentType.CREDIT_NOTE).currency(CURRENCY_USD).total(new BigDecimal(100.01)).parentDocumentNumber("1001").build());
        documents.add(Document.builder("1005", DocumentType.DEBIT_NOTE).currency(CURRENCY_EUR).total(new BigDecimal(100.01)).parentDocumentNumber("1003").build());

        BigDecimal totalSum = calculateService.getDocumentsTotalSum(documents, CURRENCY_EUR, exchangeRateList);
        BigDecimal expectedTotalSum = new BigDecimal("189.12");
        assertEquals(expectedTotalSum, totalSum);
    }

    @Test(expected = DocumentValidationException.class)
    public void missingParentDocument() throws Exception {
        List<Document> documents = new LinkedList<>();
        documents.add(Document.builder("1003", DocumentType.CREDIT_NOTE).currency(CURRENCY_USD).total(new BigDecimal(100.01)).parentDocumentNumber("1000").build());

        calculateService.getDocumentsTotalSum(documents, CURRENCY_EUR, exchangeRateList);
    }
}
