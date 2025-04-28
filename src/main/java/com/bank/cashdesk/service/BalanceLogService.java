package com.bank.cashdesk.service;

import com.bank.cashdesk.bo.CashiersRepository;
import com.bank.cashdesk.config.AppPropertiesConfig;
import com.bank.cashdesk.model.*;
import com.bank.cashdesk.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class BalanceLogService {

    @Autowired
    private AppPropertiesConfig propertiesConfig;

    @Autowired
    private CashiersRepository cashiersRepository;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public List<BalanceHistory> readBalanceHistory(LocalDateTime dateFrom,
                                                   LocalDateTime dateTo,
                                                   String cashierName) {
        List<BalanceHistory> balanceHistoryList = new ArrayList<>();

        readWriteLock.readLock().lock();

        try (BufferedReader reader = new BufferedReader(new FileReader(propertiesConfig.getBalanceFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], DateTimeUtils.getDateTimeFormatter());
                String cashier = parts[1];

                if (cashierName != null && !cashier.equalsIgnoreCase(cashierName)) {
                    continue;
                }

                if ((dateFrom != null && timestamp.isBefore(dateFrom)) || (dateTo != null && timestamp.isAfter(dateTo))) {
                    continue;
                }

                BalanceHistory balanceHistory;
                Map<CurrencyCode, CurrencyBalance> currencyBalance = new HashMap<>();

                for (int i = 2; i < parts.length; i += 3) {
                    CurrencyCode currency = CurrencyCode.valueOf(parts[i]);
                    BigInteger amount = new BigInteger(parts[i + 1]);
                    List<Denomination> denominations = parseDenominations(parts[i + 2]);

                    currencyBalance.put(
                            currency, CurrencyBalance.builder().totalAmount(amount).denominations(denominations).build());
                }

                balanceHistory = BalanceHistory
                        .builder()
                        .cashierName(cashier)
                        .timestamp(timestamp)
                        .currencyBalances(currencyBalance)
                        .build();

                balanceHistoryList.add(balanceHistory);
            }
        } catch (IOException e) {
            log.error("Error reading balance history file", e);
        } finally {
            readWriteLock.readLock().unlock();
        }
        return balanceHistoryList;
    }

    public void readFromFile(String filePath) {
        readWriteLock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            log.error("I/O error reading file: {}", filePath, e);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(";");
        String cashierName = parts[1];
        Cashier cashier = Cashier.builder().name(cashierName).build();

        for (int i = 2; i < parts.length; i += 3) {
            CurrencyCode currency = CurrencyCode.valueOf(parts[i]);
            BigInteger amount = new BigInteger(parts[i + 1]);
            List<Denomination> denominations = parseDenominations(parts[i + 2]);

            CurrencyBalance currencyBalance = new CurrencyBalance(amount, denominations);
            cashier.getBalances().put(currency, currencyBalance);
        }

        cashiersRepository.addCashier(cashierName, cashier);
    }

    private List<Denomination> parseDenominations(String denominationString) {
        List<Denomination> denominations = new ArrayList<>();
        String[] parts = denominationString.substring(1, denominationString.length() - 1).split(", ");
        for (String part : parts) {
            String[] denominationParts = part.split("x");
            int quantity = Integer.parseInt(denominationParts[0]);
            int value = Integer.parseInt(denominationParts[1]);
            denominations.add(new Denomination(value, quantity));
        }
        return denominations;
    }

    public void appendBalanceLog(Map<String, Cashier> cashiers) {
        readWriteLock.writeLock().lock();
        File logFile = new File(propertiesConfig.getBalanceFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            for (Cashier cashier : cashiers.values()) {
                StringBuilder line = new StringBuilder();
                line.append(LocalDateTime.now().format(DateTimeUtils.getDateTimeFormatter()))
                        .append(";")
                        .append(cashier.getName());

                for (CurrencyCode currency : cashier.getBalances().keySet()) {
                    CurrencyBalance balance = cashier.getBalances().get(currency);
                    String denominations = balance.getDenominations().toString();
                    line.append(";")
                            .append(currency)
                            .append(";")
                            .append(balance.getTotalAmount())
                            .append(";")
                            .append(denominations);
                }
                writer.write(line.toString());
                writer.newLine();
            }
            log.info("Balances saved to file: {}", propertiesConfig.getBalanceFile());
        } catch (IOException e) {
            log.error("Error writing balance data to file", e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

}


