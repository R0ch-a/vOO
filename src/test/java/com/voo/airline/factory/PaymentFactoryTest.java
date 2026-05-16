package com.voo.airline.factory;

import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Payment;
import com.voo.airline.enums.PaymentMethod;
import com.voo.airline.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Testa o PaymentFactory — criação e validações de pagamento.
 */
@DisplayName("PaymentFactory — testes do factory de pagamentos")
class PaymentFactoryTest {

    private PaymentFactory factory;
    private Booking        booking;

    @BeforeEach
    void setUp() {
        factory = new PaymentFactory();
        booking = mock(Booking.class);
    }

    @Test
    @DisplayName("Deve criar Payment com status PENDING")
    void createPaymentPending() {
        Payment p = factory.create(booking, PaymentMethod.PIX, new BigDecimal("700.00"));

        assertThat(p).isNotNull();
        assertThat(p.getMethod()).isEqualTo(PaymentMethod.PIX);
        assertThat(p.getAmount()).isEqualByComparingTo(new BigDecimal("700.00"));
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(p.isPaid()).isFalse();
    }

    @Test
    @DisplayName("Boleto não deve aceitar valores abaixo de R$ 50")
    void boletoMinValueFails() {
        assertThatThrownBy(() ->
            factory.create(booking, PaymentMethod.BOLETO, new BigDecimal("30.00"))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Boleto não disponível");
    }

    @Test
    @DisplayName("Boleto deve aceitar valores acima de R$ 50")
    void boletoAboveMinValueSucceeds() {
        assertThatNoException().isThrownBy(() ->
            factory.create(booking, PaymentMethod.BOLETO, new BigDecimal("50.00"))
        );
    }

    @Test
    @DisplayName("markAsPaid deve atualizar status e paidAt")
    void markAsPaid() {
        Payment p = factory.create(booking, PaymentMethod.CREDIT_CARD, new BigDecimal("350.00"));
        p.markAsPaid();

        assertThat(p.isPaid()).isTrue();
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(p.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsFailed deve atualizar status para FAILED")
    void markAsFailed() {
        Payment p = factory.create(booking, PaymentMethod.PIX, new BigDecimal("350.00"));
        p.markAsFailed();

        assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(p.isPaid()).isFalse();
    }
}
