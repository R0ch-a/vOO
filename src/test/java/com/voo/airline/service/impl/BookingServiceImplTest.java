package com.voo.airline.service.impl;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.entity.Payment;
import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import com.voo.airline.enums.PaymentMethod;
import com.voo.airline.exception.ResourceNotFoundException;
import com.voo.airline.factory.PaymentFactory;
import com.voo.airline.factory.PriceStrategyFactory;
import com.voo.airline.mapper.BookingMapper;
import com.voo.airline.observer.event.BookingCancelledEvent;
import com.voo.airline.observer.event.BookingCreatedEvent;
import com.voo.airline.repository.BookingRepository;
import com.voo.airline.repository.PassengerRepository;
import com.voo.airline.repository.PaymentRepository;
import com.voo.airline.strategy.pricing.PriceStrategy;
import com.voo.airline.validator.BookingValidatorChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl — testes unitários")
class BookingServiceImplTest {

    @Mock BookingRepository        bookingRepository;
    @Mock PassengerRepository      passengerRepository;
    @Mock PaymentRepository        paymentRepository;
    @Mock BookingValidatorChain    validatorChain;
    @Mock PriceStrategyFactory     priceStrategyFactory;
    @Mock PaymentFactory           paymentFactory;
    @Mock BookingMapper            bookingMapper;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock PriceStrategy            priceStrategy;

    @InjectMocks
    BookingServiceImpl service;

    private Passenger passenger;
    private Booking   booking;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        passenger = Passenger.of("Maria Souza", "maria@email.com", null,
                "98765432100", null, null, null);

        booking = Booking.create(
                "VOOTST01", "VO1001",
                "GIG", "LHR",
                LocalDate.now().plusDays(10), null,
                FlightType.ONEWAY, FlightClass.ECONOMY,
                "12A", "B5", "Boeing 737",
                "14:00", "13:30",
                BigDecimal.valueOf(320), passenger);

        bookingResponse = new BookingResponse(
                1L, "VOOTST01", "VO1001",
                "GIG", "LHR",
                LocalDate.now().plusDays(10), null,
                FlightType.ONEWAY, FlightClass.ECONOMY,
                "12A", "B5", "Boeing 737",
                "14:00", "13:30",
                BigDecimal.valueOf(320),
                BookingStatus.CONFIRMED, null, null);
    }

    // ── create() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private CreateBookingRequest buildRequest() {
            PassengerDataRequest pdr = new PassengerDataRequest(
                    "Maria Souza", "maria@email.com", null,
                    "98765432100", null, null, null);
            return new CreateBookingRequest(
                    null, "VO1001", "GIG", "LHR",
                    LocalDate.now().plusDays(10), null,
                    FlightType.ONEWAY, FlightClass.ECONOMY,
                    "12A", "B5", "Boeing 737",
                    "14:00", "13:30",
                    PaymentMethod.CREDIT_CARD, pdr);
        }

        @BeforeEach
        void stubMocks() {
            when(priceStrategyFactory.getStrategy(FlightClass.ECONOMY)).thenReturn(priceStrategy);
            when(priceStrategy.calculate(any(), anyInt())).thenReturn(BigDecimal.valueOf(320));
            when(priceStrategy.getStrategyName()).thenReturn("Economy");
            when(passengerRepository.findByCpf("98765432100")).thenReturn(Optional.of(passenger));
            when(bookingRepository.existsByLocator(anyString())).thenReturn(false);
            when(bookingMapper.toEntity(any(), anyString(), any(), any())).thenReturn(booking);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(paymentFactory.create(any(), any(), any())).thenReturn(mock(Payment.class));
            when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);
        }

        @Test
        @DisplayName("deve executar a cadeia de validação")
        void callsValidatorChain() {
            service.create(buildRequest());
            verify(validatorChain).validateAll(any());
        }

        @Test
        @DisplayName("deve reutilizar passageiro existente pelo CPF")
        void reusesExistingPassenger() {
            service.create(buildRequest());
            verify(passengerRepository).findByCpf("98765432100");
            verify(passengerRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve criar novo passageiro quando CPF não existe")
        void createsNewPassengerWhenNotFound() {
            when(passengerRepository.findByCpf("98765432100")).thenReturn(Optional.empty());
            when(passengerRepository.save(any())).thenReturn(passenger);
            service.create(buildRequest());
            verify(passengerRepository).save(any(Passenger.class));
        }

        @Test
        @DisplayName("deve calcular preço via Strategy")
        void calculatesPriceViaStrategy() {
            service.create(buildRequest());
            verify(priceStrategyFactory).getStrategy(FlightClass.ECONOMY);
            verify(priceStrategy).calculate(FlightType.ONEWAY, 1);
        }

        @Test
        @DisplayName("deve salvar booking e payment")
        void savesBookingAndPayment() {
            service.create(buildRequest());
            verify(bookingRepository).save(any(Booking.class));
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("deve publicar BookingCreatedEvent")
        void publishesCreatedEvent() {
            service.create(buildRequest());
            ArgumentCaptor<BookingCreatedEvent> captor =
                    ArgumentCaptor.forClass(BookingCreatedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(BookingCreatedEvent.class);
        }

        @Test
        @DisplayName("deve retornar BookingResponse")
        void returnsBookingResponse() {
            BookingResponse result = service.create(buildRequest());
            assertThat(result).isNotNull();
            assertThat(result.locator()).isEqualTo("VOOTST01");
        }
    }

    // ── findByLocator() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByLocator()")
    class FindByLocatorTests {

        @Test
        @DisplayName("deve retornar response quando localizador existe")
        void returnsResponseWhenFound() {
            when(bookingRepository.findByLocator("VOOTST01")).thenReturn(Optional.of(booking));
            when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

            BookingResponse result = service.findByLocator("VOOTST01");

            assertThat(result.locator()).isEqualTo("VOOTST01");
        }

        @Test
        @DisplayName("deve normalizar localizador para maiúsculo")
        void normalizesToUpperCase() {
            when(bookingRepository.findByLocator("VOOTST01")).thenReturn(Optional.of(booking));
            when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

            service.findByLocator("vootst01");

            verify(bookingRepository).findByLocator("VOOTST01");
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não existe")
        void throwsWhenNotFound() {
            when(bookingRepository.findByLocator(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByLocator("VOOXXXXX"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── findAll() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("deve retornar página de reservas")
        void returnsPage() {
            PageRequest pageable = PageRequest.of(0, 20);
            Page<Booking> page = new PageImpl<>(List.of(booking));
            when(bookingRepository.findAllWithPassenger(pageable)).thenReturn(page);
            when(bookingMapper.toResponse(booking)).thenReturn(bookingResponse);

            Page<BookingResponse> result = service.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // ── cancel() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("deve cancelar reserva e salvar")
        void cancelsAndSaves() {
            when(bookingRepository.findByLocator("VOOTST01")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenReturn(booking);
            when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

            service.cancel("VOOTST01");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(bookingRepository).save(booking);
        }

        @Test
        @DisplayName("deve publicar BookingCancelledEvent")
        void publishesCancelledEvent() {
            when(bookingRepository.findByLocator("VOOTST01")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenReturn(booking);
            when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

            service.cancel("VOOTST01");

            ArgumentCaptor<BookingCancelledEvent> captor =
                    ArgumentCaptor.forClass(BookingCancelledEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(BookingCancelledEvent.class);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não existe")
        void throwsWhenNotFound() {
            when(bookingRepository.findByLocator(anyString())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.cancel("VOOXXXXX"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── complete() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("deve concluir reserva e salvar")
        void completesAndSaves() {
            when(bookingRepository.findByLocator("VOOTST01")).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenReturn(booking);
            when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

            service.complete("VOOTST01");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
            verify(bookingRepository).save(booking);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando não existe")
        void throwsWhenNotFound() {
            when(bookingRepository.findByLocator(anyString())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.complete("VOOXXXXX"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
