package com.bank.cashdesk.startup;

import com.bank.cashdesk.bo.CashiersRepository;
import com.bank.cashdesk.config.AppPropertiesConfig;
import com.bank.cashdesk.model.*;
import com.bank.cashdesk.service.BalanceLogService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@Getter
@Slf4j
public class CashierInitializer implements CommandLineRunner {

    @Autowired
    private AppPropertiesConfig propertiesConfig;

    @Autowired
    private CashiersRepository cashiersRepository;

    @Autowired
    private BalanceLogService balanceLogService;

    private final String MARTINA = "MARTINA";
    private final String LINDA = "LINDA";
    private final String PETER = "PETER";

    @Override
    public void run(String... args) {
        initializeCashiers();
    }

    private void initializeCashiers() {
        Path path = Paths.get(propertiesConfig.getBalanceFile());

        if (Files.exists(path)) {
            balanceLogService.readFromFile(propertiesConfig.getBalanceFile());
        } else {
            initializeWithDefaultData();
        }
    }

    private void initializeWithDefaultData() {
        addDefaultCashier(MARTINA);
        addDefaultCashier(PETER);
        addDefaultCashier(LINDA);

        log.info("Initialized 3 cashiers (MARTINA, PETER, LINDA) with independent starting balances.");

        balanceLogService.appendBalanceLog(cashiersRepository.getCashiers());
    }

    private void addDefaultCashier(String name) {
        cashiersRepository.addCashier(name, Cashier.builder()
                .name(name)
                .balances(createNewBalances())
                .build());
    }

    private Map<CurrencyCode, CurrencyBalance> createNewBalances() {
        Map<CurrencyCode, CurrencyBalance> balances = new HashMap<>();

        balances.put(CurrencyCode.BGN, CurrencyBalance.builder()
                .totalAmount(BigInteger.valueOf(1000))
                .denominations(List.of(
                        new Denomination(10, 50),
                        new Denomination(50, 10)
                ))
                .build());

        balances.put(CurrencyCode.EUR, CurrencyBalance.builder()
                .totalAmount(BigInteger.valueOf(2000))
                .denominations(List.of(
                        new Denomination(10, 100),
                        new Denomination(50, 20)
                ))
                .build());

        return balances;
    }
}
