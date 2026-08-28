package com.carl.trading.service;

import com.carl.trading.mapper.ExchangeMapper;
import com.carl.trading.mapper.SecurityMapper;
import com.carl.trading.model.Exchange;
import com.carl.trading.web.dto.ExchangeDto;
import com.carl.trading.web.dto.SecurityDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MarketService {

    private final ExchangeMapper exchangeMapper;
    private final SecurityMapper securityMapper;

    public MarketService(ExchangeMapper exchangeMapper, SecurityMapper securityMapper) {
        this.exchangeMapper = exchangeMapper;
        this.securityMapper = securityMapper;
    }

    public List<ExchangeDto> enabledExchanges() {
        return exchangeMapper.findEnabled().stream().map(ExchangeDto::from).toList();
    }

    public List<SecurityDto> securitiesForExchange(String exchangeCode) {
        Exchange exchange = exchangeMapper.findByCode(exchangeCode);
        if (exchange == null || !exchange.enabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Unknown or disabled exchange: " + exchangeCode);
        }
        return securityMapper.findByExchangeCode(exchangeCode).stream().map(SecurityDto::from).toList();
    }
}
