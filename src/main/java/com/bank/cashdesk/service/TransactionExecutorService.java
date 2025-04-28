package com.bank.cashdesk.service;

import com.bank.cashdesk.bo.CashiersRepository;
import com.bank.cashdesk.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionExecutorService {

    @Autowired
    private BalanceLogService balanceService;

    @Autowired
    private TransactionLogService transactionHistoryService;

    @Autowired
    private CashiersRepository cashiersRepository;

    /**
     * Actual logic executed sequentially inside the worker thread.
     */
    public Transaction executeTransaction(Transaction transaction) {
        Cashier cashier = cashiersRepository.getCashier(transaction.getCashierName());

        if (cashier == null) {
            throw new IllegalArgumentException("No cashier found with the provided name.");
        }

        TransactionType type = transaction.getType();
        CurrencyCode currencyCode = transaction.getCurrency();
        List<Denomination> denominations = transaction.getDenominations();
        transaction.setTimestamp(LocalDateTime.now());

        if (type == TransactionType.DEPOSIT) {
            deposit(cashier, currencyCode, denominations);
        } else if (type == TransactionType.WITHDRAWAL) {
            withdraw(cashier, currencyCode, denominations);
        } else {
            throw new IllegalArgumentException("Unsupported transaction type");
        }

        transactionHistoryService.appendTransactionLog(transaction);
        balanceService.appendBalanceLog(Map.of(cashier.getName(), cashier));

        log.info("Transaction executed successfully for cashier {}", cashier.getName());

        return transaction;
    }

    private void deposit(Cashier cashier, CurrencyCode currencyCode, List<Denomination> denominations) {
        synchronized (cashier) {
            CurrencyBalance balance = cashier.getBalances().get(currencyCode);

            int totalDeposit = denominations.stream().mapToInt(d -> d.getValue() * d.getQuantity()).sum();
            balance.setTotalAmount(balance.getTotalAmount().add(BigInteger.valueOf(totalDeposit)));

            List<Denomination> currentDenominations = new ArrayList<>(balance.getDenominations());
            balance.setDenominations(currentDenominations);

            // Merge denominations
            for (Denomination newDenom : denominations) {
                boolean found = false;

                for (Denomination existing : balance.getDenominations()) {
                    if (existing.getValue() == newDenom.getValue()) {
                        existing.setQuantity(existing.getQuantity() + newDenom.getQuantity());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    balance.getDenominations()
                            .add(Denomination.builder()
                                    .value(newDenom.getValue())
                                    .quantity(newDenom.getQuantity())
                                    .build());
                }
            }

            log.info("Deposited {} to {}'s {} balance (denominations updated)",
                    totalDeposit, cashier.getName(), currencyCode);
        }
    }

    private void withdraw(Cashier cashier, CurrencyCode currencyCode, List<Denomination> denominations) {
        synchronized (cashier) {
            CurrencyBalance balance = cashier.getBalances().get(currencyCode);
            List<Denomination> currentDenominations = balance.getDenominations();

            BigInteger totalWithdrawal = BigInteger.valueOf(
                    denominations.stream().mapToInt(d -> d.getValue() * d.getQuantity()).sum());

            if (balance.getTotalAmount().compareTo(totalWithdrawal) < 0) {
                throw new IllegalArgumentException("Insufficient funds for withdrawal");
            }

            for (Denomination requested : denominations) {
                // Find the corresponding available denomination by value
                Denomination available = currentDenominations.stream()
                        .filter(d -> d.getValue() == requested.getValue())
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format(
                                        "Requested denomination of value %d not found. Available denominations are: %s",
                                        requested.getValue(), currentDenominations.stream().map(Denomination::getValue)
                                                .collect(Collectors.toList()))
                        ));

                if (available.getQuantity() < requested.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient quantity");
                }

                available.setQuantity(available.getQuantity() - requested.getQuantity());

                if (available.getQuantity() == 0) {
                    currentDenominations.remove(available);
                }
            }

            balance.setTotalAmount(balance.getTotalAmount().subtract(totalWithdrawal));

            log.info("Withdrawal {} to {}'s {} balance (denominations updated)",
                    totalWithdrawal, cashier.getName(), currencyCode);
        }
    }

}
