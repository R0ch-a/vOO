package com.voo.airline.strategy.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia de preço para a classe Economy.
 *
 * <p>Herda toda a lógica do Template Method de {@link AbstractPriceStrategy}.
 * Sua única responsabilidade é declarar o preço base da cabine.
 */
@Component("economyPriceStrategy")
public class EconomyPriceStrategy extends AbstractPriceStrategy {

    private static final BigDecimal BASE_PRICE = new BigDecimal("350.00");

    @Override
    public BigDecimal getBasePrice() {
        return BASE_PRICE;
    }

    @Override
    public String getStrategyName() {
        return "Economy — Tarifa Padrão";
    }
}
