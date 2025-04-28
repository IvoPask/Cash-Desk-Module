package com.bank.cashdesk.model;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Cashier {

    private String name;
    @Builder.Default
    private Map<CurrencyCode, CurrencyBalance> balances = new HashMap<>();

}
