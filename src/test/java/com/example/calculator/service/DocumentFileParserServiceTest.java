package com.example.calculator.service;

import com.example.calculator.exception.DocumentValidationException;
import com.example.calculator.model.Customer;
import com.example.calculator.model.Document;
import com.example.calculator.model.DocumentType;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import static com.example.calculator.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocumentFileParserServiceTest {

    private DocumentsFileParserService documentsFileParserService;

    @Before
    public void setup() {
        documentsFileParserService = new DocumentsFileParserService();
    }

    @Test
    public void success() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/data.csv");
        Map<String, Customer> customerMap = documentsFileParserService.parseDocumentsCsvInputStream(inputStream, null);
        assertNotNull(customerMap);
        assertEquals(3, customerMap.size());

        Customer customer1 = customerMap.get(VENDOR_1_VAT);
        assertNotNull(customer1);
        assertEquals(VENDOR_1_NAME, customer1.getName());
        assertEquals(4, customer1.getDocuments().size());

        Document document = customer1.getDocuments().stream().filter(it -> it.getDocumentNumber().equals("1000000260")).findFirst().orElse(null);
        assertNotNull(document);
        assertEquals("1000000257", document.getParentDocumentNumber());
        assertEquals(DocumentType.CREDIT_NOTE, document.getDocumentType());
        assertEquals(CURRENCY_EUR, document.getCurrency());
        assertEquals(new BigDecimal("100"), document.getTotal());

        Customer customer2 = customerMap.get(VENDOR_2_VAT);
        assertNotNull(customer2);
        assertEquals(VENDOR_2_NAME, customer2.getName());
        assertEquals(2, customer2.getDocuments().size());

        Customer customer3 = customerMap.get(VENDOR_3_VAT);
        assertNotNull(customer3);
        assertEquals(VENDOR_3_NAME, customer3.getName());
        assertEquals(2, customer3.getDocuments().size());
    }

    @Test
    public void successFilterVat() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/data.csv");
        Map<String, Customer> customerMap = documentsFileParserService.parseDocumentsCsvInputStream(inputStream, VENDOR_1_VAT);
        assertNotNull(customerMap);
        assertEquals(1, customerMap.size());

        Customer customer1 = customerMap.get(VENDOR_1_VAT);
        assertNotNull(customer1);
        assertEquals(VENDOR_1_NAME, customer1.getName());
        assertEquals(4, customer1.getDocuments().size());
    }

    @Test(expected = DocumentValidationException.class)
    public void successCsvDataError() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/data_error.csv");
        documentsFileParserService.parseDocumentsCsvInputStream(inputStream, VENDOR_1_VAT);
    }

}
