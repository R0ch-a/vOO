package com.voo.airline.entity;

import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Booking — testes de regras de negócio")
class BookingTest {

    private Passenger passenger;

    @BeforeEach
    void setUp() {
        passenger = Passenger.of("João Silva", "joao@email.com", null,
                "12345678901", null, null, null);
    }

    private Booking buildBooking(BookingStatus status) {
        Booking b = Booking.create(
                "VOOTST01", "VO1001",
                "GIG", "LHR",
                LocalDate.now().plusDays(10), null,
                FlightType.ONEWAY, FlightClass.ECONOMY,
                "12A", "B5", "Boeing 737-800",
                "14:00", "13:30",
                BigDecimal.valueOf(320), passenger);
        // Force non-CONFIRMED status via reflection when needed
        if (status == BookingStatus.CANCELLED) b.cancel();
        if (status == BookingStatus.COMPLETED) b.complete();
        return b;
    }

    // ── cancel() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("deve cancelar reserva CONFIRMED com sucesso")
        void cancelConfirmed() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            b.cancel();
            assertThat(b.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("deve lançar IllegalStateException ao cancelar reserva já cancelada")
        void cancelAlreadyCancelled() {
            Booking b = buildBooking(BookingStatus.CANCELLED);
            assertThatThrownBy(b::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já está cancelada");
        }
    }

    // ── complete() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("deve concluir reserva CONFIRMED com sucesso")
        void completeConfirmed() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            b.complete();
            assertThat(b.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        }

        @Test
        @DisplayName("deve lançar IllegalStateException ao concluir reserva cancelada")
        void completeCancelled() {
            Booking b = buildBooking(BookingStatus.CANCELLED);
            assertThatThrownBy(b::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Somente reservas confirmadas");
        }

        @Test
        @DisplayName("deve lançar IllegalStateException ao concluir reserva já completada")
        void completeAlreadyCompleted() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            b.complete();
            assertThatThrownBy(b::complete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Somente reservas confirmadas");
        }
    }

    // ── isCancellable() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("isCancellable()")
    class IsCancellableTests {

        @Test
        @DisplayName("deve retornar true para status CONFIRMED")
        void cancellableWhenConfirmed() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            assertThat(b.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para status CANCELLED")
        void notCancellableWhenCancelled() {
            Booking b = buildBooking(BookingStatus.CANCELLED);
            assertThat(b.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para status COMPLETED")
        void notCancellableWhenCompleted() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            b.complete();
            assertThat(b.isCancellable()).isFalse();
        }
    }

    // ── isRoundTrip() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isRoundTrip()")
    class IsRoundTripTests {

        @Test
        @DisplayName("deve retornar true para ROUNDTRIP")
        void roundTripTrue() {
            Booking b = Booking.create(
                    "VOORT01", "VO2001",
                    "GIG", "LHR",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(15),
                    FlightType.ROUNDTRIP, FlightClass.EXECUTIVE,
                    "01A", "A1", "Boeing 787-9",
                    "08:00", "07:30",
                    BigDecimal.valueOf(1240), passenger);
            assertThat(b.isRoundTrip()).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para ONEWAY")
        void oneWayFalse() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            assertThat(b.isRoundTrip()).isFalse();
        }
    }

    // ── create() — factory method ─────────────────────────────────────────────

    @Nested
    @DisplayName("Booking.create()")
    class CreateTests {

        @Test
        @DisplayName("deve criar reserva com status CONFIRMED por padrão")
        void defaultStatusIsConfirmed() {
            Booking b = buildBooking(BookingStatus.CONFIRMED);
            assertThat(b.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("deve normalizar origem e destino para maiúsculo")
        void normalizesOriginAndDestination() {
            Booking b = Booking.create(
                    "VOOLOW01", "VO3001",
                    "gig", "lhr",
                    LocalDate.now().plusDays(3), null,
                    FlightType.ONEWAY, FlightClass.ECONOMY,
                    "15B", "C3", "A320neo",
                    "10:00", "09:30",
                    BigDecimal.valueOf(320), passenger);
            assertThat(b.getOrigin()).isEqualTo("GIG");
            assertThat(b.getDestination()).isEqualTo("LHR");
        }

        @Test
        @DisplayName("deve usar ONEWAY quando flightType é null")
        void defaultsToOneWayWhenNull() {
            Booking b = Booking.create(
                    "VOONULL1", "VO4001",
                    "REC", "DXB",
                    LocalDate.now().plusDays(7), null,
                    null, FlightClass.PREMIUM_ECONOMY,
                    "08C", "D4", "A321",
                    "22:00", "21:30",
                    BigDecimal.valueOf(980), passenger);
            assertThat(b.getFlightType()).isEqualTo(FlightType.ONEWAY);
        }
    }
}
