package com.bank.cashdesk.service;

import com.bank.cashdesk.config.AppPropertiesConfig;
import com.bank.cashdesk.model.Transaction;
import com.bank.cashdesk.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class TransactionLogService {

    @Autowired
    private AppPropertiesConfig propertiesConfig;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void appendTransactionLog(Transaction transaction) {
        readWriteLock.writeLock().lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesConfig.getTransactionFile(), true))) {

            StringBuilder transactionLog = new StringBuilder();
            transactionLog.append(transaction.getTimestamp().format(DateTimeUtils.getDateTimeFormatter()))
                    .append(";")
                    .append(transaction.getCashierName())
                    .append(";")
                    .append(transaction.getCurrency())
                    .append(";")
                    .append(transaction.getType())
                    .append(";")
                    .append(transaction.getDenominations().stream()
                            .mapToInt(d -> d.getValue() * d.getQuantity())
                            .sum())
                    .append(";")
                    .append(transaction.getDenominations())
                    .append("\n");

            writer.write(transactionLog.toString());
            log.info("Transaction saved to file: {}", propertiesConfig.getTransactionFile());
        } catch (IOException e) {
            log.error("Error writing transaction to file", e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

}
