package com.carl.trading.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioDto(
        BigDecimal cashBalanceUsd,
        BigDecimal holdingsMarketValueUsd,
        BigDecimal totalAccountValueUsd,
        BigDecimal totalCostBasisUsd,
        BigDecimal totalUnrealizedPlUsd,
        List<HoldingDto> holdings) {
}
