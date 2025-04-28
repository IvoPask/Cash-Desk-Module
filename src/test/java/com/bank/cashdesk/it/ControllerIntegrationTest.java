package com.bank.cashdesk.it;

import com.bank.cashdesk.config.AppPropertiesConfig;
import com.bank.cashdesk.dto.DenominationDTO;
import com.bank.cashdesk.dto.TransactionDTO;
import com.bank.cashdesk.model.CurrencyCode;
import com.bank.cashdesk.model.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class ControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppPropertiesConfig appPropertiesConfig;

    @Test
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
    void testGetBalanceByCashierName() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/cash-balance")
                        .queryParam("cashier", "martina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(appPropertiesConfig.getRequestHeader(), appPropertiesConfig.getApiKey()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.[0].cashierName").value("MARTINA"))
                .andDo(print());
    }

    @Test
    void testDeposit() throws Exception {

        //TODO: get initial balance

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

        //TODO: get balance after deposit anc check
    }

    @Test
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

    //TODO: Write tests

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
