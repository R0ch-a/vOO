package com.voo.airline.strategy.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia de preço para a classe Executiva.
 *
 * <p>Demonstra o poder do Template Method + Herança:
 * <ul>
 *   <li>Sobrescreve {@link #getRoundTripMultiplier()} — executiva tem
 *       desconto de 20% no trecho de volta (fidelização premium)</li>
 *   <li>Sobrescreve {@link #applyGroupDiscount(BigDecimal, int)} —
 *       a classe executiva não oferece desconto de grupo</li>
 * </ul>
 */
@Component("executivePriceStrategy")
public class ExecutivePriceStrategy extends AbstractPriceStrategy {

    private static final BigDecimal BASE_PRICE            = new BigDecimal("1800.00");
    private static final BigDecimal ROUNDTRIP_MULTIPLIER  = new BigDecimal("1.80"); // 10% off na volta

    @Override
    public BigDecimal getBasePrice() {
        return BASE_PRICE;
    }

    /**
     * Executiva oferece 10% de desconto no trecho de retorno.
     * Total = base * 1.8 em vez de base * 2.
     */
    @Override
    protected BigDecimal getRoundTripMultiplier() {
        return ROUNDTRIP_MULTIPLIER;
    }

    /**
     * Executiva não aplica desconto de grupo — sobrescreve o hook
     * retornando o preço sem modificação.
     */
    @Override
    protected BigDecimal applyGroupDiscount(BigDecimal price, int passengers) {
        return price; // sem desconto de grupo na executiva
    }

    @Override
    public String getStrategyName() {
        return "Executiva — Tarifa Premium";
    }
}
