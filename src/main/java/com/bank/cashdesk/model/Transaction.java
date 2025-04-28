package com.bank.cashdesk.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    private String cashierName;
    private CurrencyCode currency;
    private TransactionType type;
    @Builder.Default
    private List<Denomination> denominations = new ArrayList<>();
    private LocalDateTime timestamp;

}
