package com.bank.cashdesk.controller;

import com.bank.cashdesk.dto.BalanceHistoryDTO;
import com.bank.cashdesk.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class CashBalanceController {

    @Autowired
    private BalanceService balanceService;

    @GetMapping("/cash-balance")
    @ResponseStatus(HttpStatus.OK)
    public List<BalanceHistoryDTO> getBalance(@RequestParam(value = "dateFrom", required = false) String dateFrom,
                                              @RequestParam(value = "dateTo", required = false) String dateTo,
                                              @RequestParam(value = "cashier", required = false) String cashierName) {

        return BalanceHistoryDTO.ofModel(balanceService.getBalance(cashierName, dateFrom, dateTo));
    }


}
