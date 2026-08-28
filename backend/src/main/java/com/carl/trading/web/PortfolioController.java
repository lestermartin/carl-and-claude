package com.carl.trading.web;

import com.carl.trading.security.CurrentCustomer;
import com.carl.trading.service.PortfolioService;
import com.carl.trading.web.dto.PortfolioDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CurrentCustomer currentCustomer;

    public PortfolioController(PortfolioService portfolioService, CurrentCustomer currentCustomer) {
        this.portfolioService = portfolioService;
        this.currentCustomer = currentCustomer;
    }

    @GetMapping
    public PortfolioDto portfolio() {
        return portfolioService.forCustomer(currentCustomer.require());
    }
}
