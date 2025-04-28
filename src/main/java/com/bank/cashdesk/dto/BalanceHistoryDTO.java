package com.bank.cashdesk.dto;

import com.bank.cashdesk.model.BalanceHistory;
import com.bank.cashdesk.model.CurrencyCode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BalanceHistoryDTO(

        @NotNull(message = "Cashier name must not be null")
        String cashierName,

        @NotNull(message = "Timestamp must not be null")
        LocalDateTime timestamp,

        @NotNull(message = "Currency balances must not be null")
        Map<CurrencyCode, CurrencyBalanceDTO> currencyBalances) {

    public static BalanceHistoryDTO ofModel(BalanceHistory balanceHistory) {
        return new BalanceHistoryDTO(
                balanceHistory.getCashierName(),
                balanceHistory.getTimestamp(),
                balanceHistory.getCurrencyBalances().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> CurrencyBalanceDTO.ofModel(entry.getValue())
                        ))
        );
    }

    public static List<BalanceHistoryDTO> ofModel(List<BalanceHistory> balanceHistories) {
        return balanceHistories.stream()
                .map(BalanceHistoryDTO::ofModel)
                .collect(Collectors.toList());
    }

}
