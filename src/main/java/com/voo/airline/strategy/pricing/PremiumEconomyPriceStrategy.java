package com.voo.airline.strategy.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia de preço para a classe Premium Economy.
 */
@Component("premiumEconomyPriceStrategy")
public class PremiumEconomyPriceStrategy extends AbstractPriceStrategy {

    private static final BigDecimal BASE_PRICE = new BigDecimal("750.00");

    @Override
    public BigDecimal getBasePrice() {
        return BASE_PRICE;
    }

    @Override
    public String getStrategyName() {
        return "Premium Economy — Tarifa Intermediária";
    }
}
