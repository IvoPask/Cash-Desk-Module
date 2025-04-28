package com.bank.cashdesk.bo;

import com.bank.cashdesk.model.Cashier;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class CashiersRepository {

    @Builder.Default
    private Map<String, Cashier> cashiers = new HashMap<>();

    public void addCashier(String name, Cashier cashier) {
        cashiers.put(name, cashier);
    }

    public Cashier getCashier(String name) {
        return cashiers.get(name);
    }

}
