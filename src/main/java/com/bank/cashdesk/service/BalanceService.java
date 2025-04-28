package com.bank.cashdesk.service;

import com.bank.cashdesk.bo.CashiersRepository;
import com.bank.cashdesk.model.BalanceHistory;
import com.bank.cashdesk.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BalanceService {

    @Autowired
    private BalanceLogService balanceLogService;

    @Autowired
    private CashiersRepository cashiersRepository;

    public List<BalanceHistory> getBalance(String cashierName, String dateFrom, String dateTo) {

        LocalDateTime from = DateTimeUtils.parseDate(dateFrom, "dateFrom");
        LocalDateTime to = DateTimeUtils.parseDate(dateTo, "dateTo");

        if (cashierName != null && !cashiersRepository.getCashiers().containsKey(cashierName.toUpperCase())) {
            throw new IllegalArgumentException("Cashier not found");
        }

        return balanceLogService.readBalanceHistory(from, to, cashierName);

    }

}
