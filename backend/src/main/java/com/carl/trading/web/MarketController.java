package com.carl.trading.web;

import com.carl.trading.service.MarketService;
import com.carl.trading.web.dto.ExchangeDto;
import com.carl.trading.web.dto.SecurityDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/exchanges")
    public List<ExchangeDto> exchanges() {
        return marketService.enabledExchanges();
    }

    @GetMapping("/securities")
    public List<SecurityDto> securities(@RequestParam("exchange") String exchangeCode) {
        return marketService.securitiesForExchange(exchangeCode);
    }
}
