package com.bank.cashdesk.service;

import com.bank.cashdesk.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class CashierQueueService {

    @Autowired
    private TransactionExecutorService transactionExecutorService;

    private final Map<String, ExecutorService> cashierExecutors = new ConcurrentHashMap<>();

    public CompletableFuture<Transaction> submitTransaction(Transaction transaction) {
        String cashierName = transaction.getCashierName();
        cashierExecutors.putIfAbsent(cashierName, Executors.newSingleThreadExecutor());

        ExecutorService executor = cashierExecutors.get(cashierName);

        return CompletableFuture.supplyAsync(() -> transactionExecutorService.executeTransaction(transaction), executor);
    }

}