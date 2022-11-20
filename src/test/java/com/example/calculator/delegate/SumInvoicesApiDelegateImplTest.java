package com.example.calculator.delegate;

import com.example.calculator.service.CalculateService;
import com.example.calculator.service.CurrencyExchangeService;
import com.example.calculator.service.DocumentsFileParserService;
import com.example.calculator.specification.model.CalculateResponseDto;
import com.example.calculator.specification.model.CustomerDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import static com.example.calculator.TestConstants.*;

public class SumInvoicesApiDelegateImplTest {

    private SumInvoicesApiDelegateImpl delegate;
    private List<String> exchangeRates;


    @Before
    public void setup() {
        CurrencyExchangeService currencyExchangeService = new CurrencyExchangeService();
        CalculateService calculateService = new CalculateService(currencyExchangeService);
        DocumentsFileParserService documentsFileParserService = new DocumentsFileParserService();
        delegate = new SumInvoicesApiDelegateImpl(calculateService, documentsFileParserService);

        exchangeRates = Arrays.asList("EUR:1", "USD:0.987", "GBP:0.878");
    }

    @Test(expected = ResponseStatusException.class)
    public void invalidOutputCurrencyNotMatchingRegex() {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        List<String> exchangeRates = new LinkedList<>();
        delegate.sumInvoices(multipartFile, exchangeRates, "asddsasd", null);
    }

    @Test(expected = ResponseStatusException.class)
    public void invalidOutputCurrencyNonIso() {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        List<String> exchangeRates = new LinkedList<>();
        delegate.sumInvoices(multipartFile, exchangeRates, "XYZ", null);
    }

    @Test(expected = ResponseStatusException.class)
    public void unsupportedCurrencyInExchangeRates() {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        List<String> exchangeRates = Arrays.asList("EUR:1", "XYZ:0.322");
        delegate.sumInvoices(multipartFile, exchangeRates, "EUR", null);
    }

    @Test(expected = ResponseStatusException.class)
    public void invalidExchangeRateValues() {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        List<String> exchangeRates = Arrays.asList("EUR:1", "XYZ");
        delegate.sumInvoices(multipartFile, exchangeRates, "EUR", null);
    }

    @Test
    public void success() throws IOException {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getInputStream()).thenReturn(this.getClass().getResourceAsStream("/data.csv"));
        Mockito.when(multipartFile.getName()).thenReturn("data.csv");
        ResponseEntity<CalculateResponseDto> response = delegate.sumInvoices(multipartFile, exchangeRates, "EUR", null);
        assertNotNull(response);
        CalculateResponseDto responseDto = response.getBody();
        assertEquals("EUR", responseDto.getCurrency());
        List<CustomerDto> customerDtoList = responseDto.getCustomers();
        assertEquals(3, customerDtoList.size());

        CustomerDto vendor1 = customerDtoList.stream().filter(it -> it.getName().equals(VENDOR_1_NAME)).findFirst().orElse(null);
        assertNotNull(vendor1);
        assertEquals(new BigDecimal("1938.70"), vendor1.getBalance());

        CustomerDto vendor2 = customerDtoList.stream().filter(it -> it.getName().equals(VENDOR_2_NAME)).findFirst().orElse(null);
        assertNotNull(vendor2);
        assertEquals(new BigDecimal("702.60"), vendor2.getBalance());

        CustomerDto vendor3 = customerDtoList.stream().filter(it -> it.getName().equals(VENDOR_3_NAME)).findFirst().orElse(null);
        assertNotNull(vendor3);
        assertEquals(new BigDecimal("1241.40"), vendor3.getBalance());
    }

    @Test
    public void successFilterByVat() throws IOException {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getInputStream()).thenReturn(this.getClass().getResourceAsStream("/data.csv"));
        Mockito.when(multipartFile.getName()).thenReturn("data.csv");
        ResponseEntity<CalculateResponseDto> response = delegate.sumInvoices(multipartFile, exchangeRates, "EUR", VENDOR_1_VAT);
        assertNotNull(response);
        CalculateResponseDto responseDto = response.getBody();
        assertEquals("EUR", responseDto.getCurrency());
        List<CustomerDto> customerDtoList = responseDto.getCustomers();
        assertEquals(1, customerDtoList.size());
        assertEquals(VENDOR_1_NAME, customerDtoList.get(0).getName());

        CustomerDto vendor1 = customerDtoList.stream().filter(it -> it.getName().equals(VENDOR_1_NAME)).findFirst().orElse(null);
        assertNotNull(vendor1);
        assertEquals(new BigDecimal("1938.70"), vendor1.getBalance());
    }

    @Test(expected = ResponseStatusException.class)
    public void testInvalidCsvFile() throws IOException {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getInputStream()).thenReturn(this.getClass().getResourceAsStream("/test.png"));
        Mockito.when(multipartFile.getName()).thenReturn("test.png");
        delegate.sumInvoices(multipartFile, exchangeRates, "EUR", null);
    }

    @Test(expected = ResponseStatusException.class)
    public void testInvalidCsvDataError() throws IOException {
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getInputStream()).thenReturn(this.getClass().getResourceAsStream("/data_error.csv"));
        Mockito.when(multipartFile.getName()).thenReturn("data_error.csv");
        delegate.sumInvoices(multipartFile, exchangeRates, "EUR", null);
    }
}
