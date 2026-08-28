package com.carl.trading.web;

import com.carl.trading.security.CurrentCustomer;
import com.carl.trading.service.TransactionService;
import com.carl.trading.web.dto.TransactionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentCustomer currentCustomer;

    public TransactionController(TransactionService transactionService, CurrentCustomer currentCustomer) {
        this.transactionService = transactionService;
        this.currentCustomer = currentCustomer;
    }

    @GetMapping
    public List<TransactionDto> transactions() {
        return transactionService.forCustomer(currentCustomer.require());
    }
}
