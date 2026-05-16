package com.voo.airline.strategy;

import com.voo.airline.enums.FlightType;
import com.voo.airline.strategy.pricing.EconomyPriceStrategy;
import com.voo.airline.strategy.pricing.ExecutivePriceStrategy;
import com.voo.airline.strategy.pricing.PremiumEconomyPriceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa as três implementações do padrão Strategy de precificação
 * e o comportamento do Template Method da classe abstrata.
 */
@DisplayName("PriceStrategy — testes das estratégias de preço")
class PriceStrategyTest {

    private EconomyPriceStrategy      economy;
    private PremiumEconomyPriceStrategy premium;
    private ExecutivePriceStrategy    executive;

    @BeforeEach
    void setUp() {
        economy   = new EconomyPriceStrategy();
        premium   = new PremiumEconomyPriceStrategy();
        executive = new ExecutivePriceStrategy();
    }

    // ---- ECONOMY ----
    @Nested
    @DisplayName("Economy")
    class EconomyTests {

        @Test
        @DisplayName("Preço base deve ser R$ 350,00")
        void basePrice() {
            assertThat(economy.getBasePrice())
                .isEqualByComparingTo(new BigDecimal("350.00"));
        }

        @Test
        @DisplayName("Ida simples, 1 passageiro = R$ 350,00")
        void onewayOnePassenger() {
            BigDecimal result = economy.calculate(FlightType.ONEWAY, 1);
            assertThat(result).isEqualByComparingTo(new BigDecimal("350.00"));
        }

        @Test
        @DisplayName("Ida e volta, 1 passageiro = R$ 700,00")
        void roundtripOnePassenger() {
            BigDecimal result = economy.calculate(FlightType.ROUNDTRIP, 1);
            assertThat(result).isEqualByComparingTo(new BigDecimal("700.00"));
        }

        @Test
        @DisplayName("Template Method: desconto de grupo 5% para 3+ passageiros")
        void groupDiscount() {
            // 3 pax * 350 = 1050 - 5% = 997,50
            BigDecimal result = economy.calculate(FlightType.ONEWAY, 3);
            assertThat(result).isEqualByComparingTo(new BigDecimal("997.50"));
        }

        @Test
        @DisplayName("2 passageiros não recebem desconto de grupo")
        void noGroupDiscountFor2() {
            BigDecimal result = economy.calculate(FlightType.ONEWAY, 2);
            assertThat(result).isEqualByComparingTo(new BigDecimal("700.00"));
        }
    }

    // ---- PREMIUM ECONOMY ----
    @Nested
    @DisplayName("Premium Economy")
    class PremiumTests {

        @Test
        @DisplayName("Preço base deve ser R$ 750,00")
        void basePrice() {
            assertThat(premium.getBasePrice())
                .isEqualByComparingTo(new BigDecimal("750.00"));
        }

        @Test
        @DisplayName("Ida e volta, 1 passageiro = R$ 1.500,00")
        void roundtrip() {
            assertThat(premium.calculate(FlightType.ROUNDTRIP, 1))
                .isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Desconto de grupo para 3 passageiros")
        void groupDiscount() {
            // 3 * 750 = 2250 - 5% = 2137,50
            assertThat(premium.calculate(FlightType.ONEWAY, 3))
                .isEqualByComparingTo(new BigDecimal("2137.50"));
        }
    }

    // ---- EXECUTIVA ----
    @Nested
    @DisplayName("Executiva")
    class ExecutiveTests {

        @Test
        @DisplayName("Preço base deve ser R$ 1.800,00")
        void basePrice() {
            assertThat(executive.getBasePrice())
                .isEqualByComparingTo(new BigDecimal("1800.00"));
        }

        @Test
        @DisplayName("Ida simples = R$ 1.800,00")
        void oneway() {
            assertThat(executive.calculate(FlightType.ONEWAY, 1))
                .isEqualByComparingTo(new BigDecimal("1800.00"));
        }

        @Test
        @DisplayName("Ida e volta com multiplicador 1.8x = R$ 3.240,00")
        void roundtripWithDiscount() {
            // executiva: 1800 * 1.8 = 3240 (não 3600)
            assertThat(executive.calculate(FlightType.ROUNDTRIP, 1))
                .isEqualByComparingTo(new BigDecimal("3240.00"));
        }

        @Test
        @DisplayName("Executiva não aplica desconto de grupo (hook sobrescrito)")
        void noGroupDiscount() {
            // sem desconto: 3 * 1800 = 5400
            assertThat(executive.calculate(FlightType.ONEWAY, 3))
                .isEqualByComparingTo(new BigDecimal("5400.00"));
        }
    }

    // ---- Polimorfismo ----
    @Test
    @DisplayName("Polimorfismo: mesma chamada, resultados diferentes por estratégia")
    void polymorphism() {
        var e = economy.calculate(FlightType.ONEWAY, 1);
        var p = premium.calculate(FlightType.ONEWAY, 1);
        var x = executive.calculate(FlightType.ONEWAY, 1);

        assertThat(e).isLessThan(p);
        assertThat(p).isLessThan(x);
    }
}
