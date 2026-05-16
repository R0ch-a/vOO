package com.voo.airline.strategy.pricing;

import com.voo.airline.enums.FlightType;

import java.math.BigDecimal;

/**
 * Interface da Strategy de cálculo de preço.
 *
 * <p><b>Padrão Strategy:</b> define o contrato para o cálculo de tarifas.
 * Cada classe concreta encapsula o algoritmo de precificação de uma
 * classe de cabine, permitindo trocar a estratégia em tempo de execução
 * sem alterar o código que a usa.
 *
 * <p>Princípio OCP (Open/Closed): para adicionar uma nova classe de cabine,
 * basta criar uma nova implementação desta interface — sem modificar nada.
 */
public interface PriceStrategy {

    /**
     * Calcula o preço total da passagem.
     *
     * @param flightType tipo do voo (ONEWAY ou ROUNDTRIP)
     * @param passengers número de passageiros
     * @return preço total calculado
     */
    BigDecimal calculate(FlightType flightType, int passengers);

    /** Retorna o preço base por passageiro para exibição. */
    BigDecimal getBasePrice();

    /** Nome legível da estratégia, para logs e respostas. */
    String getStrategyName();
}
