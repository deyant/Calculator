package com.example.calculator.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.calculator.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SumInvoicesApiControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void success() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "EUR")
                        .param("exchangeRates", "EUR:1", "USD:0.987", "GBP:0.878"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currency", is("EUR")))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers", hasSize(3)))
                .andExpect(jsonPath("$.customers[*].name", contains(VENDOR_1_NAME, VENDOR_2_NAME, VENDOR_3_NAME)));
    }

    @Test
    public void successWithVatFilter() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "USD")
                        .param("exchangeRates", "EUR:1", "USD:0.987", "GBP:0.878")
                        .param("customerVat", VENDOR_1_VAT))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers", hasSize(1)))
                .andExpect(jsonPath("$.customers[0].name", is(VENDOR_1_NAME)));
    }

    @Test
    public void failedValidationOutputCurrencyRegex() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "asd")
                        .param("exchangeRates", "EUR:1", "USD:0.987", "GBP:0.878"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failedValidationOutputCurrencyInvalid() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "XYZ")
                        .param("exchangeRates", "EUR:1", "USD:0.987", "GBP:0.878"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failedValidationExchangeRates() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "EUR")
                        .param("exchangeRates", "EUR:1", "USD:::ABC"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void invalid_CSV_data() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "data_error.csv",
                "text/csv",
                this.getClass().getResourceAsStream("/data_error.csv"));

        mvc.perform(multipart("/api/v1/sumInvoices")
                        .file(multipartFile)
                        .param("outputCurrency", "EUR")
                        .param("exchangeRates", "EUR:1", "USD:0.987", "GBP:0.878"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
