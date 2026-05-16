package com.voo.airline.strategy.pricing;

import com.voo.airline.enums.FlightType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classe abstrata que implementa o <b>Template Method</b> para cálculo de preços.
 *
 * <p>Define o <i>esqueleto</i> do algoritmo de precificação no método
 * {@link #calculate}, que é {@code final} — garantindo que as subclasses
 * não possam quebrar a lógica geral. Os pontos de variação são delegados
 * aos métodos abstratos {@link #getBasePrice()} e {@link #getRoundTripMultiplier()}.
 *
 * <p><b>Herança + Abstração:</b> subclasses concretas como {@link EconomyPriceStrategy}
 * fornecem apenas os valores específicos; o Template cuida do resto.
 */
public abstract class AbstractPriceStrategy implements PriceStrategy {

    private static final BigDecimal PASSENGER_DISCOUNT_THRESHOLD = BigDecimal.valueOf(3);
    private static final BigDecimal GROUP_DISCOUNT_RATE           = new BigDecimal("0.05");

    /**
     * Template Method — algoritmo fixo de cálculo de preço.
     * <ol>
     *   <li>Obtém o preço base da subclasse</li>
     *   <li>Aplica multiplicador de ida e volta (se aplicável)</li>
     *   <li>Multiplica pelo número de passageiros</li>
     *   <li>Aplica desconto de grupo (se aplicável) — hook</li>
     * </ol>
     */
    @Override
    public final BigDecimal calculate(FlightType flightType, int passengers) {
        BigDecimal price = getBasePrice();

        // Passo 2: multiplica para roundtrip
        if (flightType == FlightType.ROUNDTRIP) {
            price = price.multiply(getRoundTripMultiplier());
        }

        // Passo 3: multiplica pelos passageiros
        price = price.multiply(BigDecimal.valueOf(passengers));

        // Passo 4: hook de desconto (pode ser sobrescrito pelas subclasses)
        price = applyGroupDiscount(price, passengers);

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Multiplicador para voos de ida e volta.
     * Por padrão é 2x, mas subclasses como Executiva podem ter valor diferente.
     */
    protected BigDecimal getRoundTripMultiplier() {
        return BigDecimal.valueOf(2);
    }

    /**
     * Hook Method — desconto de grupo para 3+ passageiros.
     * Subclasses podem sobrescrever para alterar ou desabilitar.
     */
    protected BigDecimal applyGroupDiscount(BigDecimal price, int passengers) {
        if (BigDecimal.valueOf(passengers).compareTo(PASSENGER_DISCOUNT_THRESHOLD) >= 0) {
            BigDecimal discount = price.multiply(GROUP_DISCOUNT_RATE);
            return price.subtract(discount);
        }
        return price;
    }
}
