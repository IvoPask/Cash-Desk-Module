package com.bank.cashdesk.dto;

import com.bank.cashdesk.model.CurrencyCode;
import com.bank.cashdesk.model.Transaction;
import com.bank.cashdesk.model.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TransactionDTO(

        @NotBlank(message = "Cashier name is required")
        String cashierName,

        @NotNull(message = "Operation type is required")
        TransactionType type,

        @NotNull(message = "Currency is required")
        CurrencyCode currencyCode,

        @Valid
        @NotEmpty(message = "Denominations list must not be empty")
        List<DenominationDTO> denominations) {

    public static TransactionDTO ofModel(Transaction transaction) {
        return new TransactionDTO(
                transaction.getCashierName(), transaction.getType(),
                transaction.getCurrency(), DenominationDTO.ofModel(transaction.getDenominations()));
    }

    public static Transaction of(TransactionDTO transactionDTO) {
        return Transaction
                .builder()
                .cashierName(transactionDTO.cashierName())
                .type(transactionDTO.type())
                .currency(transactionDTO.currencyCode())
                .denominations(DenominationDTO.of(transactionDTO.denominations()))
                .build();
    }

}
