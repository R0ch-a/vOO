package com.voo.airline.factory;

import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Payment;
import com.voo.airline.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Factory responsável por criar instâncias de {@link Payment}.
 *
 * <p><b>Padrão Factory:</b> isola a lógica de criação do Payment do Service,
 * garantindo que toda criação passe por regras centralizadas (ex: validar
 * se o método de pagamento aceita o valor mínimo).
 *
 * <p>Caso a lógica de criação de Payment evolua (ex: diferentes tipos de
 * transação para Pix vs. Cartão), este é o único ponto a modificar.
 */
@Component
public class PaymentFactory {

    private static final BigDecimal BOLETO_MIN_VALUE = new BigDecimal("50.00");

    /**
     * Cria um Payment para a booking informada.
     *
     * @throws IllegalArgumentException se o método não for adequado ao valor
     */
    public Payment create(Booking booking, PaymentMethod method, BigDecimal amount) {
        validate(method, amount);
        return Payment.of(booking, method, amount);
    }

    private void validate(PaymentMethod method, BigDecimal amount) {
        if (method == PaymentMethod.BOLETO
                && amount.compareTo(BOLETO_MIN_VALUE) < 0) {
            throw new IllegalArgumentException(
                "Boleto não disponível para valores abaixo de R$ " + BOLETO_MIN_VALUE);
        }
    }
}
