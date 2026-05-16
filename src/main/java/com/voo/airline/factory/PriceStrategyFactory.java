package com.voo.airline.factory;

import com.voo.airline.enums.FlightClass;
import com.voo.airline.strategy.pricing.PriceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory responsável por retornar a {@link PriceStrategy} correta
 * de acordo com a {@link FlightClass} informada.
 *
 * <p><b>Padrão Factory:</b> centraliza a criação/resolução de objetos Strategy,
 * desacoplando o código cliente (Service) de saber qual implementação usar.
 * O Spring injeta o mapa de strategies automaticamente pelo nome do bean.
 *
 * <p><b>Polimorfismo:</b> o chamador recebe sempre um {@code PriceStrategy}
 * e nunca precisa fazer instanceof — apenas chama {@code calculate()}.
 */
@Component
@RequiredArgsConstructor
public class PriceStrategyFactory {

    private final Map<String, PriceStrategy> strategies;

    /**
     * Retorna a estratégia de preço para a classe de cabine indicada.
     *
     * @param flightClass classe do voo
     * @return implementação concreta de PriceStrategy
     * @throws IllegalArgumentException se a classe não tiver estratégia mapeada
     */
    public PriceStrategy getStrategy(FlightClass flightClass) {
        String beanName = switch (flightClass) {
            case ECONOMY         -> "economyPriceStrategy";
            case PREMIUM_ECONOMY -> "premiumEconomyPriceStrategy";
            case EXECUTIVE       -> "executivePriceStrategy";
        };

        PriceStrategy strategy = strategies.get(beanName);

        if (strategy == null) {
            throw new IllegalArgumentException(
                "Nenhuma estratégia de preço encontrada para: " + flightClass);
        }

        return strategy;
    }
}
