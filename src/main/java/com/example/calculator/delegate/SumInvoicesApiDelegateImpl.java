package com.example.calculator.delegate;

import com.example.calculator.exception.CurrencyExchangeException;
import com.example.calculator.exception.DocumentValidationException;
import com.example.calculator.exception.UnsupportedCurrencyException;
import com.example.calculator.model.Customer;
import com.example.calculator.model.ExchangeRate;
import com.example.calculator.service.CalculateService;
import com.example.calculator.service.DocumentsFileParserService;
import com.example.calculator.specification.api.SumInvoicesApiDelegate;
import com.example.calculator.specification.model.CalculateResponseDto;
import com.example.calculator.specification.model.CustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of {@link com.example.calculator.specification.api.SumInvoicesApiDelegate SumInvoicesApiDelegate}
 */
@Slf4j
@Component
public class SumInvoicesApiDelegateImpl implements SumInvoicesApiDelegate {

    private CalculateService calculateService;
    private DocumentsFileParserService documentsFileParserService;
    private static final Pattern PATTERN_EXCHANGE_RATE = Pattern.compile("^([A-Z]){3}:((\\d{1,})|(\\d{1,}\\.\\d{1,}))$");
    private static final Pattern PATTERN_CURRENCY_CODE = Pattern.compile("^([A-Z]){3}$");

    public SumInvoicesApiDelegateImpl(CalculateService calculateService, DocumentsFileParserService documentsFileParserService) {
        this.calculateService = calculateService;
        this.documentsFileParserService = documentsFileParserService;
    }

    /**
     * Sum the invoices in the document, using the provided output currency and currency exchange rates.
     *
     * @param file The CSV file, containing a list of invoices, debit and credit notes in different currencies. (required)
     * @param exchangeRates A list of currencies and exchange rates (for example: EUR:1,USD:0.987,GBP:0.878)  (required)
     * @param outputCurrency ISO 4217 currency code (required)
     * @param customerVat This the optional input filter. If specified, the result should contain only one customer matching the one specified in this filter.  (optional)
     * @return
     */
    @Override
    public ResponseEntity<CalculateResponseDto> sumInvoices(MultipartFile file,
                                                            List<String> exchangeRates,
                                                            String outputCurrency,
                                                            String customerVat) {

        final Currency outputCurrencyObject = getOutputCurrency(outputCurrency);
        final Collection<ExchangeRate> exchangeRateSet;
        try {
            exchangeRateSet = buildExchangeRates(exchangeRates);
        } catch (UnsupportedCurrencyException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        final Map<String, Customer> customersMap;
        try {
            customersMap = documentsFileParserService.parseDocumentsCsvInputStream(file.getInputStream(), customerVat);
        } catch (IOException | IllegalArgumentException e) {
            log.info("Error parsing CSV file [{}]: {}", file.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to parse CSV file " + file.getName() + ": " + e.getMessage());
        } catch (DocumentValidationException docEx) {
            log.info("Validation failed for document [{}]: {}", docEx.getDocumentNumber(), docEx.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Validation failed for document [%s]: [%s]", docEx.getDocumentNumber(), docEx.getMessage()));
        }

        List<CustomerDto> customers = new LinkedList<>();
        for (Customer customer : customersMap.values()) {
            BigDecimal totalBalance;
            try {
                totalBalance = calculateService.getDocumentsTotalSum(
                        customer.getDocuments(), outputCurrencyObject, exchangeRateSet);
            } catch (CurrencyExchangeException currEx) {
                log.info("Error while calculating documents total sum for customer with VAT [{}]: {}",
                        customer.getVatNumber(), currEx.getMessage());

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Currency conversion error from [%s] to [%s]: %s",
                                currEx.getFromCurrency(), currEx.getToCurrency(), currEx.getMessage()));
            } catch (DocumentValidationException docValEx) {
                log.info("Error while calculating documents total sum for customer with VAT [{}]: {}",
                        customer.getVatNumber(), docValEx.getMessage());

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Failed validation for document [%s] and customer with VAT number [%s]: %s",
                                docValEx.getDocumentNumber(), customer.getVatNumber(), docValEx.getMessage()));
            }
            CustomerDto customerDto = new CustomerDto();
            customerDto.setName(customer.getName());
            customerDto.setBalance(totalBalance);
            customers.add(customerDto);
        }

        CalculateResponseDto responseDto = new CalculateResponseDto();
        responseDto.setCurrency(outputCurrency);
        responseDto.setCustomers(customers);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Parse string exchange rates and build a collection of
     * {@link com.example.calculator.model.ExchangeRate ExchangeRate}
     * instances
     * @param exchangeRateStringList
     * @return A collection {@link com.example.calculator.model.ExchangeRate ExchangeRate} models
     * @throws UnsupportedCurrencyException If an invalid currency code is provided
     */
    private Collection<ExchangeRate> buildExchangeRates(List<String> exchangeRateStringList) throws UnsupportedCurrencyException {
        Set<ExchangeRate> exchangeRates = new HashSet<>();
        for (String exchangeRateString : exchangeRateStringList) {
            if (StringUtils.isBlank(exchangeRateString)) {
                continue;
            }

            if (!PATTERN_EXCHANGE_RATE.matcher(exchangeRateString).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Invalid currency exchange rate: [%s]. Examples: 'EUR:1' or 'GBP:0.123'", exchangeRateString));
            }

            String[] rateSplit = exchangeRateString.split(":");
            BigDecimal rateBigDecimal = new BigDecimal(rateSplit[1]);
            Currency currency;
            try {
                currency = Currency.getInstance(rateSplit[0]);
            } catch (IllegalArgumentException e) {
                throw new UnsupportedCurrencyException(rateSplit[0]);
            }
            ExchangeRate exchangeRate = new ExchangeRate(currency, rateBigDecimal);

            Optional<ExchangeRate> existingExchangeRate = exchangeRates.stream()
                    .filter(exchangeRate1 -> exchangeRate1.getCurrency().equals(exchangeRate.getCurrency()))
                    .findFirst();

            if (existingExchangeRate.isPresent() && !existingExchangeRate.get().equals(exchangeRate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Multiple exchange rates provided for currency: %s", exchangeRate.getCurrency().getCurrencyCode()));
            } else {
                exchangeRates.add(exchangeRate);
            }
        }

        // Validate default currency
        long countDefaultCurrencies = exchangeRates.stream().filter(exchangeRate -> exchangeRate.getRate().equals(BigDecimal.ONE)).count();
        if (countDefaultCurrencies == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default exchange rate currency is not specified");
        }

        if (countDefaultCurrencies > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Expected only 1 default exchange rate currency, while %d are specified", countDefaultCurrencies));
        }
        return exchangeRates;
    }

    /**
     * Create a {@link java.util.Currency Currency} using currency code.
     *
     * @param outputCurrency Currency code
     * @return An instance of {@link java.util.Currency Currency}
     * @throws ResponseStatusException If requested outputCurrency is not valid
     */
    private Currency getOutputCurrency(String outputCurrency) throws ResponseStatusException {
        // Workaround check, OpenAPI generator for some reason did not add @Pattern
        // constraint for outputCurrency to SumInvoicesApi
        if (!PATTERN_CURRENCY_CODE.matcher(outputCurrency).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "outputCurrency does not match the regex " + PATTERN_CURRENCY_CODE);
        }
        try {
            return Currency.getInstance(outputCurrency);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported ISO 4217 currency code: " + outputCurrency);
        }
    }
}
