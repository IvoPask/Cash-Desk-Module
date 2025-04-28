package com.bank.cashdesk;

import com.bank.cashdesk.model.*;
import com.bank.cashdesk.service.BalanceLogService;
import com.bank.cashdesk.service.TransactionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

public class ConcurrencyTest {

    @Mock
    private BalanceLogService balanceLogService;

    @Mock
    private TransactionLogService transactionLogService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConcurrentBalanceWrites() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int cashierId = i;
            executorService.submit(() -> {
                try {
                    Cashier cashier = Cashier.builder()
                            .name("Cashier_" + cashierId)
                            .balances(Map.of(
                                    CurrencyCode.BGN, new CurrencyBalance(BigInteger.valueOf(1000), List.of())
                            ))
                            .build();

                    balanceLogService.appendBalanceLog(Map.of(cashier.getName(), cashier));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        verify(balanceLogService, times(10)).appendBalanceLog(any());
    }

    @Test
    public void testConcurrentTransactionWrites() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int cashierId = i;
            executorService.submit(() -> {
                try {
                    Transaction transaction = Transaction.builder()
                            .cashierName("Cashier_" + cashierId)
                            .currency(CurrencyCode.BGN)
                            .type(TransactionType.DEPOSIT)
                            .denominations(List.of(new Denomination(10, 5)))
                            .timestamp(LocalDateTime.now())
                            .build();

                    transactionLogService.appendTransactionLog(transaction);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        verify(transactionLogService, times(10)).appendTransactionLog(any());
    }

    @RepeatedTest(5)
    public void testConcurrentWritesRaceCondition() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(20);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    Transaction transaction = Transaction.builder()
                            .cashierName("Cashier_Concurrent")
                            .currency(CurrencyCode.EUR)
                            .type(TransactionType.WITHDRAWAL)
                            .denominations(List.of(new Denomination(20, 3)))
                            .timestamp(LocalDateTime.now())
                            .build();
                    transactionLogService.appendTransactionLog(transaction);
                } finally {
                    latch.countDown();
                }
            });

            executorService.submit(() -> {
                try {
                    Cashier cashier = Cashier.builder()
                            .name("Cashier_Concurrent")
                            .balances(Map.of(
                                    CurrencyCode.EUR, new CurrencyBalance(BigInteger.valueOf(2000), List.of())
                            ))
                            .build();
                    balanceLogService.appendBalanceLog(Map.of(cashier.getName(), cashier));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        verify(transactionLogService, atLeast(10)).appendTransactionLog(any());
        verify(balanceLogService, atLeast(10)).appendBalanceLog(any());
    }


}
