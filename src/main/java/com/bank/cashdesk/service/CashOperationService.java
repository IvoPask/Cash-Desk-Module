package com.bank.cashdesk.service;

import com.bank.cashdesk.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CashOperationService {

    @Autowired
    private CashierQueueService cashierQueueService;

    public Transaction performOperation(Transaction transaction) {
        try {
            // Submit and wait for completion
            return cashierQueueService.submitTransaction(transaction).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Transaction execution was interrupted", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Transaction execution failed", e.getCause());
        }
    }

}
