package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import com.voo.airline.enums.PaymentMethod;
import com.voo.airline.exception.BusinessException;
import com.voo.airline.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testa a cadeia de validadores (Chain of Responsibility + Template Method).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingValidatorChain — testes da cadeia de validação")
class BookingValidatorChainTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingValidatorChain chain;

    private PassengerDataRequest passenger;

    @BeforeEach
    void setUp() {
        RouteValidator            routeValidator   = new RouteValidator();
        RoundTripDateValidator    dateValidator    = new RoundTripDateValidator();
        SeatAvailabilityValidator seatValidator    = new SeatAvailabilityValidator(bookingRepository);

        chain = new BookingValidatorChain(routeValidator, dateValidator, seatValidator);

        passenger = new PassengerDataRequest(
            "Ana Silva", "ana@email.com", "(79) 99999-9999",
            "12345678900", LocalDate.of(1990, 1, 1), null, null
        );
    }

    @Test
    @DisplayName("Request válido deve passar por toda a cadeia sem exceção")
    void validRequestPassesAll() {
        when(bookingRepository.isSeatTaken(any(), any(), any(), anyString()))
            .thenReturn(false);

        var req = buildRequest("GRU", "SSA", FlightType.ONEWAY,
            LocalDate.now().plusDays(5), null, "12A");

        assertThatNoException().isThrownBy(() -> chain.validateAll(req));
    }

    @Test
    @DisplayName("RouteValidator: origem == destino deve lançar BusinessException")
    void sameOriginDestinationFails() {
        var req = buildRequest("GRU", "GRU", FlightType.ONEWAY,
            LocalDate.now().plusDays(5), null, null);

        assertThatThrownBy(() -> chain.validateAll(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("mesmo aeroporto");
    }

    @Test
    @DisplayName("RoundTripDateValidator: sem data de volta em roundtrip falha")
    void roundtripWithoutReturnDateFails() {
        var req = buildRequest("GRU", "SSA", FlightType.ROUNDTRIP,
            LocalDate.now().plusDays(5), null, null);

        assertThatThrownBy(() -> chain.validateAll(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Data de volta");
    }

    @Test
    @DisplayName("RoundTripDateValidator: data de volta anterior à ida falha")
    void returnBeforeDepartureFails() {
        var req = buildRequest("GRU", "SSA", FlightType.ROUNDTRIP,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(5), // antes!
            null);

        assertThatThrownBy(() -> chain.validateAll(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("SeatAvailabilityValidator: assento ocupado lança exceção")
    void takenSeatFails() {
        when(bookingRepository.isSeatTaken(any(), any(), any(), anyString()))
            .thenReturn(true);

        var req = buildRequest("GRU", "SSA", FlightType.ONEWAY,
            LocalDate.now().plusDays(5), null, "12A");

        assertThatThrownBy(() -> chain.validateAll(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("já está ocupado");
    }

    @Test
    @DisplayName("Template Method: cada validador tem getRuleName() definido")
    void ruleNames() {
        assertThatNoException().isThrownBy(() -> {
            var r = new RouteValidator();
            var d = new RoundTripDateValidator();
            var s = new SeatAvailabilityValidator(bookingRepository);
            assert r.getRuleName() != null && !r.getRuleName().isBlank();
            assert d.getRuleName() != null && !d.getRuleName().isBlank();
            assert s.getRuleName() != null && !s.getRuleName().isBlank();
        });
    }

    // helper
    private CreateBookingRequest buildRequest(String origin, String dest,
                                               FlightType type, LocalDate dep,
                                               LocalDate ret, String seat) {
        return new CreateBookingRequest(null, "VO1234", origin, dest,
            dep, ret, type, FlightClass.ECONOMY, seat,
            "B3", "Boeing 737", "14:30", "14:00",
            PaymentMethod.PIX, passenger);
    }
}
