package com.voo.airline;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.enums.*;
import com.voo.airline.exception.BusinessException;
import com.voo.airline.repository.BookingRepository;
import com.voo.airline.repository.PassengerRepository;
import com.voo.airline.repository.PaymentRepository;
import com.voo.airline.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — testes unitários")
class BookingServiceTest {

    @Mock BookingRepository   bookingRepository;
    @Mock PassengerRepository passengerRepository;
    @Mock PaymentRepository   paymentRepository;

    @InjectMocks BookingServiceImpl bookingService;

    private CreateBookingRequest validRequest;
    private Passenger            savedPassenger;
    private Booking              savedBooking;

    @BeforeEach
    void setUp() {
        validRequest = new CreateBookingRequest(
            null,                        // locator — gerado pelo backend
            "VO1234",                    // flightNum
            "GRU", "SSA",               // origin, destination
            LocalDate.now().plusDays(10), // depDate
            null,                        // retDate
            FlightType.ONEWAY,
            FlightClass.ECONOMY,
            "12A",                       // seat
            "B3",                        // gate
            "Boeing 737-800",            // aircraft
            "14:30", "14:00",            // departure, boarding
            PaymentMethod.PIX,
            new PassengerDataRequest(
                "Ana Carolina Souza",
                "ana@email.com",
                "(79) 99999-9999",
                "12345678900",
                LocalDate.of(1995, 5, 15),
                DocType.CPF,
                "12345678900"
            )
        );

        savedPassenger = Passenger.builder()
            .id(1L)
            .name("Ana Carolina Souza")
            .cpf("12345678900")
            .email("ana@email.com")
            .build();

        savedBooking = Booking.builder()
            .id(1L)
            .locator("VOOABC123")
            .flightNum("VO1234")
            .origin("GRU")
            .destination("SSA")
            .depDate(LocalDate.now().plusDays(10))
            .flightType(FlightType.ONEWAY)
            .flightClass(FlightClass.ECONOMY)
            .seat("12A")
            .totalPrice(new BigDecimal("350.00"))
            .status(BookingStatus.CONFIRMED)
            .passenger(savedPassenger)
            .build();
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso")
    void shouldCreateBookingSuccessfully() {
        when(bookingRepository.isSeatTaken(any(), any(), any(), any())).thenReturn(false);
        when(passengerRepository.findByCpf(any())).thenReturn(Optional.of(savedPassenger));
        when(bookingRepository.existsByLocator(any())).thenReturn(false);
        when(bookingRepository.save(any())).thenReturn(savedBooking);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BookingResponse result = bookingService.create(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.origin()).isEqualTo("GRU");
        assertThat(result.destination()).isEqualTo("SSA");
        assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, times(1)).save(any());
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando origem == destino")
    void shouldThrowWhenOriginEqualsDestination() {
        var req = new CreateBookingRequest(
            null, "VO1234",
            "GRU", "GRU",   // mesmos aeroportos
            LocalDate.now().plusDays(5), null,
            FlightType.ONEWAY, FlightClass.ECONOMY,
            "12A", "B3", "Boeing 737", "14:30", "14:00",
            PaymentMethod.PIX,
            validRequest.passengerData()
        );

        assertThatThrownBy(() -> bookingService.create(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Origem e destino não podem ser iguais");
    }

    @Test
    @DisplayName("Deve lançar exceção quando assento já está ocupado")
    void shouldThrowWhenSeatAlreadyTaken() {
        when(bookingRepository.isSeatTaken(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(validRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("já está ocupado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de volta é anterior à ida em roundtrip")
    void shouldThrowWhenRetDateBeforeDepDate() {
        var req = new CreateBookingRequest(
            null, "VO1234", "GRU", "SSA",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(5),  // volta antes da ida
            FlightType.ROUNDTRIP, FlightClass.ECONOMY,
            "12A", "B3", "Boeing 737", "14:30", "14:00",
            PaymentMethod.PIX,
            validRequest.passengerData()
        );

        assertThatThrownBy(() -> bookingService.create(req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Data de volta deve ser posterior");
    }

    @Test
    @DisplayName("Deve calcular preço dobrado para ida e volta")
    void shouldDoubleThePriceForRoundTrip() {
        var rtRequest = new CreateBookingRequest(
            null, "VO1234", "GRU", "SSA",
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(20),
            FlightType.ROUNDTRIP, FlightClass.ECONOMY,
            "12A", "B3", "Boeing 737", "14:30", "14:00",
            PaymentMethod.PIX,
            validRequest.passengerData()
        );

        Booking rtBooking = Booking.builder()
            .id(2L).locator("VOORT001").flightNum("VO1234")
            .origin("GRU").destination("SSA")
            .depDate(LocalDate.now().plusDays(10))
            .retDate(LocalDate.now().plusDays(20))
            .flightType(FlightType.ROUNDTRIP)
            .flightClass(FlightClass.ECONOMY)
            .totalPrice(new BigDecimal("700.00"))
            .status(BookingStatus.CONFIRMED)
            .passenger(savedPassenger)
            .build();

        when(bookingRepository.isSeatTaken(any(), any(), any(), any())).thenReturn(false);
        when(passengerRepository.findByCpf(any())).thenReturn(Optional.of(savedPassenger));
        when(bookingRepository.existsByLocator(any())).thenReturn(false);
        when(bookingRepository.save(any())).thenReturn(rtBooking);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BookingResponse result = bookingService.create(rtRequest);

        assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("700.00"));
    }
}
