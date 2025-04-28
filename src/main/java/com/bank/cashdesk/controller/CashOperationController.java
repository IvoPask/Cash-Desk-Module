package com.bank.cashdesk.controller;

import com.bank.cashdesk.dto.TransactionDTO;
import com.bank.cashdesk.model.Transaction;
import com.bank.cashdesk.service.CashOperationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Validated
public class CashOperationController {

    @Autowired
    private CashOperationService cashOperationService;

    @PostMapping("/cash-operation")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDTO performOperation(@Valid @RequestBody TransactionDTO transactionDTO) {

        Transaction transaction = TransactionDTO.of(transactionDTO);

        return TransactionDTO.ofModel(cashOperationService.performOperation(transaction));
    }

}
