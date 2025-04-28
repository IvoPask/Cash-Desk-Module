package com.bank.cashdesk.model;

import lombok.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyBalance {

    private BigInteger totalAmount;
    @Builder.Default
    private List<Denomination> denominations = new ArrayList<>();

}
