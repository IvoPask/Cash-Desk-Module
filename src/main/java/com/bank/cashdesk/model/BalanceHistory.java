package com.bank.cashdesk.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceHistory {

    private String cashierName;
    private LocalDateTime timestamp;
    @Builder.Default
    private Map<CurrencyCode, CurrencyBalance> currencyBalances = new HashMap<>();

}
