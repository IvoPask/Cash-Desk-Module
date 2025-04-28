package com.bank.cashdesk.it;

import com.bank.cashdesk.bo.CashiersRepository;
import com.bank.cashdesk.config.AppPropertiesConfig;
import com.bank.cashdesk.dto.DenominationDTO;
import com.bank.cashdesk.dto.TransactionDTO;
import com.bank.cashdesk.model.CurrencyCode;
import com.bank.cashdesk.model.TransactionType;
import com.bank.cashdesk.utils.DateTimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppPropertiesConfig appPropertiesConfig;

    @Autowired
    private CashiersRepository cashiersRepository;

    @Test
    @Order(1)
    void testGetBalanceAll() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/cash-balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(3))
                .andDo(print());
    }

    @Test
    @Order(2)
    void testGetBalanceByCashierName() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/cash-balance")
                        .queryParam("cashier", "martina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].cashierName").value("MARTINA"))
                .andDo(print());
    }

    @Test
    @Order(3)
    void testDeposit() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "MARTINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cashierName").value("MARTINA"))
                .andExpect(jsonPath("$[0].currencyBalances.EUR.totalAmount").value(2000))
                .andExpect(jsonPath("$[0].currencyBalances.BGN.totalAmount").value(1000));

        DenominationDTO denominationDTO = new DenominationDTO(20, 2);

        TransactionDTO transactionDTO = new TransactionDTO("MARTINA", TransactionType.DEPOSIT, CurrencyCode.EUR, List.of(denominationDTO));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "MARTINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].cashierName").value("MARTINA"))
                .andExpect(jsonPath("$[1].currencyBalances.EUR.totalAmount").value(2040))
                .andExpect(jsonPath("$[1].currencyBalances.BGN.totalAmount").value(1000))
                .andDo(print());
    }

    @Test
    @Order(4)
    void testWithdraw() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "MARTINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].cashierName").value("MARTINA"))
                .andExpect(jsonPath("$[1].currencyBalances.EUR.totalAmount").value(2040))
                .andExpect(jsonPath("$[1].currencyBalances.BGN.totalAmount").value(1000));

        DenominationDTO denominationDTO = new DenominationDTO(20, 1);

        TransactionDTO transactionDTO = new TransactionDTO("MARTINA", TransactionType.WITHDRAWAL, CurrencyCode.EUR, List.of(denominationDTO));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "MARTINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].cashierName").value("MARTINA"))
                .andExpect(jsonPath("$[2].currencyBalances.EUR.totalAmount").value(2020))
                .andExpect(jsonPath("$[2].currencyBalances.BGN.totalAmount").value(1000))
                .andDo(print());
    }

    @Test
    @Order(5)
    void testGetBalanceAllAfterTwoTransactions() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/cash-balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(5))
                .andDo(print());
    }

    @Test
    @Order(6)
    void testConcurrentCashOperationsAndBalanceReads() throws Exception {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads * 2);

        for (int i = 0; i < numberOfThreads; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    TransactionDTO transactionDTO = new TransactionDTO(
                            "MARTINA",
                            TransactionType.DEPOSIT,
                            CurrencyCode.BGN,
                            List.of(new DenominationDTO(10, 5 + index))
                    );
                    String body = objectMapper.writeValueAsString(transactionDTO);

                    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cash-operation")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                                    .content(body))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try {
                    mockMvc
                            .perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                                    .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                                    .queryParam("cashier", "MARTINA"))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Final read
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "MARTINA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cashierName").value("MARTINA"));
    }

    @Test
    void testTransactionDenominationValidation() throws Exception {

        TransactionDTO transactionDTO = new TransactionDTO("PETER", TransactionType.DEPOSIT, CurrencyCode.EUR, List.of());

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Denominations list must not be empty"))
                .andDo(print());
    }

    @Test
    void testTransactionCashierNameValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("", TransactionType.DEPOSIT, CurrencyCode.EUR,
                        List.of(new DenominationDTO(10, 1)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Cashier name is required"))
                .andDo(print());
    }

    @Test
    void testTransactionTypeValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", null, CurrencyCode.EUR,
                        List.of(new DenominationDTO(10, 1)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Operation type is required"))
                .andDo(print());
    }

    @Test
    void testTransactionCurrencyCodeValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("LINDA", TransactionType.DEPOSIT, null,
                        List.of(new DenominationDTO(10, 1)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Currency is required"))
                .andDo(print());
    }

    @Test
    void testTransactionNestedDenominationNegativeQuantityValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", TransactionType.DEPOSIT, CurrencyCode.EUR,
                        List.of(new DenominationDTO(10, -1)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("Denomination quantity must be at least 1"))
                .andDo(print());
    }

    @Test
    void testTransactionNestedDenominationNegativeValueValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", TransactionType.DEPOSIT, CurrencyCode.EUR,
                        List.of(new DenominationDTO(-10, 1)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("Denomination value must be at least 1"))
                .andDo(print());
    }

    @Test
    void testTransactionInsufficientFundsValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", TransactionType.WITHDRAWAL, CurrencyCode.EUR,
                        List.of(new DenominationDTO(10, 1000)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Insufficient funds for withdrawal"))
                .andDo(print());
    }

    @Test
    void testTransactionInsufficientQuantityValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", TransactionType.WITHDRAWAL, CurrencyCode.EUR,
                        List.of(new DenominationDTO(10, 101)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Insufficient quantity"))
                .andDo(print());
    }

    @Test
    void testTransactionAvailableDenominationsValidation() throws Exception {
        TransactionDTO transactionDTO =
                new TransactionDTO("PETER", TransactionType.WITHDRAWAL, CurrencyCode.EUR,
                        List.of(new DenominationDTO(23, 50)));

        String body = objectMapper.writeValueAsString(transactionDTO);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Requested denomination of value 23 not found. Available denominations are: [10, 50]"))
                .andDo(print());
    }

    @Test
    void testGetBalancesForCashierByDate() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "LINDA")
                        .queryParam("dateFrom", LocalDateTime.now()
                                .minusDays(1).format(DateTimeUtils.getDateTimeFormatter()))
                        .queryParam("dateTo", LocalDateTime.now()
                                .plusDays(1).format(DateTimeUtils.getDateTimeFormatter()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].cashierName").value("LINDA"))
                .andDo(print());

    }

    @Test
    void testGetBalancesInvalidDate() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "LINDA")
                        .queryParam("dateFrom", "dd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Invalid date format for 'dateFrom'. Expected format is yyyy-MM-dd'T'HH:mm:ss"))
                .andDo(print());

    }

    @Test
    void testGetBalancesInvalidCashier() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey())
                        .queryParam("cashier", "LINDAa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cashier not found"))
                .andDo(print());

    }

    @AfterAll
    void cleanUpFiles() {
        try {
            Files.deleteIfExists(Paths.get(appPropertiesConfig.getBalanceFile()));
            Files.deleteIfExists(Paths.get(appPropertiesConfig.getTransactionFile()));
            System.out.println("Test files cleaned up successfully.");
        } catch (IOException e) {
            System.err.println("Failed to clean up test files: " + e.getMessage());
        }
    }
}
