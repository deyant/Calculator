package com.example.calculator.service;

import com.example.calculator.exception.DocumentValidationException;
import com.example.calculator.model.Customer;
import com.example.calculator.model.Document;
import com.example.calculator.model.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parser of CSV files containing customers and documents.
 */
@Slf4j
@Service
public class DocumentsFileParserService {

    private CSVFormat csvFormat;

    enum CsvHeader {
        CUSTOMER("Customer"),
        VAT_NUMBER("Vat number"),
        DOCUMENT_NUMBER("Document number"),
        TYPE("Type"),
        PARENT_DOCUMENT("Parent document"),
        CURRENCY("Currency"),
        TOTAL("Total");

        private String value;

        CsvHeader(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public DocumentsFileParserService() {
        csvFormat = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true)
                .setHeader(Arrays.asList(CsvHeader.values())
                        .stream()
                        .map(CsvHeader::toString)
                        .collect(Collectors.toList()).toArray(new String[0]))
                .build();
    }

    /**
     * Parse a CSV file input and build a map of Customer VAT Numbers and Customers.
     *
     * @param inputStream     CSV file input stream
     * @param filterVatNumber Filter customers with specified VAT number (optional)
     * @return A map of VAT numbers and Customer objects.
     * @throws IOException                 If CSV input stream reading fails.
     * @throws DocumentValidationException If document line contains invalid data.
     * @throws IllegalArgumentException    If CSV structure is invalid.
     */
    public Map<String, Customer> parseDocumentsCsvInputStream(final InputStream inputStream,
                                                              final String filterVatNumber)
            throws IOException, DocumentValidationException, IllegalArgumentException {
        HashMap<String, Customer> customersMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = csvFormat.parse(reader)) {

            for (final CSVRecord record : parser) {
                final String vatNumber = record.get(CsvHeader.VAT_NUMBER.toString());
                if (filterVatNumber != null && !filterVatNumber.equals(vatNumber)) {
                    continue;
                }

                final Customer customer;
                if (customersMap.containsKey(vatNumber)) {
                    customer = customersMap.get(vatNumber);

                } else {
                    final String customerName = record.get(CsvHeader.CUSTOMER.toString());
                    customer = new Customer(customerName, vatNumber);
                    customersMap.put(vatNumber, customer);
                }

                // TODO: Check for duplicate documents by number but different values
                final String documentNumber = record.get(CsvHeader.DOCUMENT_NUMBER.toString());
                final String documentTypeString = record.get(CsvHeader.TYPE.toString());
                final int documentTypeInt;
                try {
                    documentTypeInt = Integer.parseInt(documentTypeString);
                } catch (NumberFormatException nfEx) {
                    throw new DocumentValidationException("Document type is not a number: " +
                            documentTypeString, documentNumber);
                }

                DocumentType documentType = DocumentType.valueOf(documentTypeInt);
                if (documentType == null) {
                    throw new DocumentValidationException("Invalid document type: " + documentTypeInt, documentNumber);
                }

                final String parentDocumentNumber = record.get(CsvHeader.PARENT_DOCUMENT.toString());
                final String documentCurrencyString = record.get(CsvHeader.CURRENCY.toString());
                final Currency documentCurrencyObject;
                try {
                    documentCurrencyObject = Currency.getInstance(documentCurrencyString);
                } catch (IllegalArgumentException e) {
                    throw new DocumentValidationException("Unsupported ISO 4217 currency code: " +
                            documentCurrencyString, documentNumber);
                }
                final String documentTotalString = record.get(CsvHeader.TOTAL.toString());
                final BigDecimal documentTotal;
                try {
                    documentTotal = new BigDecimal(documentTotalString);
                } catch (NumberFormatException nfEx) {
                    throw new DocumentValidationException("Document total is not a decimal number: " +
                            documentTotalString, documentNumber);
                }

                Document document = Document.builder(documentNumber, documentType)
                        .parentDocumentNumber(parentDocumentNumber)
                        .currency(documentCurrencyObject)
                        .total(documentTotal)
                        .build();

                customer.addDocument(document);
            }
        }
        return customersMap;

    }
}
