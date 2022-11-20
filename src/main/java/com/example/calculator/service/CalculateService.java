package com.example.calculator.service;

import com.example.calculator.Constants;
import com.example.calculator.exception.CurrencyExchangeException;
import com.example.calculator.exception.DocumentValidationException;
import com.example.calculator.model.Document;
import com.example.calculator.model.ExchangeRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.Objects;

/**
 * Calculates amounts of documents.
 */
@Service
public class CalculateService {

    private CurrencyExchangeService currencyExchangeService;

    public CalculateService(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    /**
     * Calculate the sum of totals for a list of {@link com.example.calculator.model.Document Documents}
     * into a specified currency using provided exchange rates.
     *
     * @param documents     The documents to calculate the sum for
     * @param currency      The currency into which the sum will be converted to.
     * @param exchangeRates Currency exchange rates.
     * @return The sum of totals of the documents in the requested currency.
     * @throws CurrencyExchangeException   If an invalid currency has been specified or exchange rate does not exist
     *                                     for a currency in the documents.
     * @throws DocumentValidationException If a document is invalid according to the business rules, e.g. missing
     *                                     parent document.
     */
    public BigDecimal getDocumentsTotalSum(final Collection<Document> documents, final Currency currency, Collection<ExchangeRate> exchangeRates)
            throws CurrencyExchangeException, DocumentValidationException {
        Objects.requireNonNull(documents, "Argument [documents] cannot be null");
        Objects.requireNonNull(currency, "Argument [currency] cannot be null");
        Objects.requireNonNull(exchangeRates, "Argument [exchangeRates] cannot be null");

        BigDecimal totalSum = BigDecimal.ZERO;
        for (Document doc : documents) {
            BigDecimal documentTotalConverted = currencyExchangeService.convertAmount(exchangeRates, doc.getCurrency(), currency, doc.getTotal());

            switch (doc.getDocumentType()) {
                case INVOICE -> totalSum = totalSum.add(documentTotalConverted);

                case CREDIT_NOTE -> {
                    if (doc.getParentDocumentNumber() == null) {
                        throw new DocumentValidationException("Parent document number is required for Credit Notes", doc.getDocumentNumber());
                    }

                    documents.stream()
                            .filter(it -> it.getDocumentNumber().equals(doc.getParentDocumentNumber()))
                            .findFirst()
                            .orElseThrow(() -> new DocumentValidationException(
                                    String.format("Non-existing parent document specified: [%s]", doc.getParentDocumentNumber()), doc.getDocumentNumber()));

                    totalSum = totalSum.subtract(documentTotalConverted);
                }

                case DEBIT_NOTE -> {
                    documents.stream()
                            .filter(it -> it.getDocumentNumber().equals(doc.getParentDocumentNumber()))
                            .findFirst()
                            .orElseThrow(() -> new DocumentValidationException(
                                    String.format("Non-existing parent document specified: [%s]", doc.getParentDocumentNumber()), doc.getDocumentNumber()));

                    totalSum = totalSum.add(documentTotalConverted);
                }
            }
        }

        return totalSum.setScale(currency.getDefaultFractionDigits(), Constants.DEFAULT_ROUNDING_MODE);
    }
}
