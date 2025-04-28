package com.bank.cashdesk.dto;

import com.bank.cashdesk.model.CurrencyBalance;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public record CurrencyBalanceDTO(

        @NotNull(message = "Total amount must not be null")
        BigInteger totalAmount,

        @NotEmpty(message = "Denominations list must not be empty")
        List<DenominationDTO> denominations

) {

    public static CurrencyBalanceDTO ofModel(CurrencyBalance currencyBalance) {
        return new CurrencyBalanceDTO(
                currencyBalance.getTotalAmount(), DenominationDTO.ofModel(currencyBalance.getDenominations()));
    }

    public static List<CurrencyBalanceDTO> ofModel(List<CurrencyBalance> currencyBalances) {
        return currencyBalances.stream()
                .map(CurrencyBalanceDTO::ofModel)
                .collect(Collectors.toList());
    }

}
