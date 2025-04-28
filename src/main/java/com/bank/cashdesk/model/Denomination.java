package com.bank.cashdesk.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Denomination {

    private int value;
    private int quantity;

    @Override
    public String toString() {
        return quantity + "x" + value;
    }
}
