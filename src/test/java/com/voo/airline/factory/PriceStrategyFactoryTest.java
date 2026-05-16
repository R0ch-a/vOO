package com.voo.airline.factory;

import com.voo.airline.enums.FlightClass;
import com.voo.airline.strategy.pricing.EconomyPriceStrategy;
import com.voo.airline.strategy.pricing.ExecutivePriceStrategy;
import com.voo.airline.strategy.pricing.PremiumEconomyPriceStrategy;
import com.voo.airline.strategy.pricing.PriceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testa o PriceStrategyFactory (padrão Factory).
 */
@DisplayName("PriceStrategyFactory — testes do factory de estratégias")
class PriceStrategyFactoryTest {

    private PriceStrategyFactory factory;

    @BeforeEach
    void setUp() {
        Map<String, PriceStrategy> strategies = Map.of(
            "economyPriceStrategy",        new EconomyPriceStrategy(),
            "premiumEconomyPriceStrategy", new PremiumEconomyPriceStrategy(),
            "executivePriceStrategy",      new ExecutivePriceStrategy()
        );
        factory = new PriceStrategyFactory(strategies);
    }

    @Test
    @DisplayName("ECONOMY deve retornar EconomyPriceStrategy")
    void economyResolvesCorrectly() {
        PriceStrategy s = factory.getStrategy(FlightClass.ECONOMY);
        assertThat(s).isInstanceOf(EconomyPriceStrategy.class);
    }

    @Test
    @DisplayName("PREMIUM_ECONOMY deve retornar PremiumEconomyPriceStrategy")
    void premiumResolvesCorrectly() {
        PriceStrategy s = factory.getStrategy(FlightClass.PREMIUM_ECONOMY);
        assertThat(s).isInstanceOf(PremiumEconomyPriceStrategy.class);
    }

    @Test
    @DisplayName("EXECUTIVE deve retornar ExecutivePriceStrategy")
    void executiveResolvesCorrectly() {
        PriceStrategy s = factory.getStrategy(FlightClass.EXECUTIVE);
        assertThat(s).isInstanceOf(ExecutivePriceStrategy.class);
    }

    @Test
    @DisplayName("Polimorfismo: todas as estratégias implementam PriceStrategy")
    void allImplementInterface() {
        for (FlightClass fc : FlightClass.values()) {
            PriceStrategy s = factory.getStrategy(fc);
            assertThat(s).isInstanceOf(PriceStrategy.class);
            assertThat(s.getStrategyName()).isNotBlank();
            assertThat(s.getBasePrice()).isPositive();
        }
    }
}
