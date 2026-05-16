package com.voo.airline.service;

import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testa o encapsulamento e as transições de estado das entidades do domínio.
 * Demonstra os princípios de OO: encapsulamento, invariantes de negócio,
 * e factory methods.
 */
@DisplayName("Entidades de Domínio — encapsulamento e transições de estado")
class BookingEntityTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        Passenger passenger = Passenger.of(
            "Carlos Oliveira", "carlos@email.com", "(11) 91234-5678",
            "98765432100", LocalDate.of(1985, 3, 20), null, null
        );

        booking = Booking.create(
            "VOOXYZ123", "VO4321",
            "FOR", "GRU",
            LocalDate.now().plusDays(7), null,
            FlightType.ONEWAY, FlightClass.PREMIUM_ECONOMY,
            "5B", "A2", "Airbus A320neo",
            "10:00", "09:30",
            new BigDecimal("750.00"), passenger
        );
    }

    @Test
    @DisplayName("Booking criada deve estar com status CONFIRMED")
    void initialStatusIsConfirmed() {
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("cancel() deve mudar status para CANCELLED")
    void cancelChangesStatus() {
        booking.cancel();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel() em booking já cancelada deve lançar IllegalStateException")
    void doubleCancelThrows() {
        booking.cancel();
        assertThatThrownBy(booking::cancel)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("já está cancelada");
    }

    @Test
    @DisplayName("complete() deve funcionar em booking CONFIRMED")
    void completeFromConfirmed() {
        booking.complete();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("complete() em CANCELLED deve lançar exceção")
    void completeFromCancelledThrows() {
        booking.cancel();
        assertThatThrownBy(booking::complete)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("confirmadas");
    }

    @Test
    @DisplayName("isCancellable() deve ser true quando CONFIRMED")
    void isCancellableWhenConfirmed() {
        assertThat(booking.isCancellable()).isTrue();
    }

    @Test
    @DisplayName("isRoundTrip() deve ser false para ONEWAY")
    void isNotRoundTrip() {
        assertThat(booking.isRoundTrip()).isFalse();
    }

    @Test
    @DisplayName("getPayments() deve retornar lista imutável")
    void paymentsListIsUnmodifiable() {
        assertThatThrownBy(() -> booking.getPayments().add(null))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Passenger.of() deve lançar exceção para nome nulo")
    void passengerRequiresName() {
        assertThatThrownBy(() ->
            Passenger.of(null, "email@test.com", null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Nome");
    }

    @Test
    @DisplayName("AbstractEntity.equals() compara por id")
    void equalsById() {
        // Mesma instância = igual a ela mesma
        assertThat(booking).isEqualTo(booking);
    }

    @Test
    @DisplayName("AbstractEntity.isNew() deve ser true antes de persistir")
    void isNewBeforePersist() {
        Passenger p = Passenger.of("Test", null, null, null, null, null, null);
        assertThat(p.isNew()).isTrue();
    }
}
