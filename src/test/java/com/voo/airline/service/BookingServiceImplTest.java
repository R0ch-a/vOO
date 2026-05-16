package com.voo.airline.service;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.enums.*;
import com.voo.airline.exception.ResourceNotFoundException;
import com.voo.airline.factory.PaymentFactory;
import com.voo.airline.factory.PriceStrategyFactory;
import com.voo.airline.mapper.BookingMapper;
import com.voo.airline.observer.event.BookingCreatedEvent;
import com.voo.airline.repository.BookingRepository;
import com.voo.airline.repository.PassengerRepository;
import com.voo.airline.repository.PaymentRepository;
import com.voo.airline.service.impl.BookingServiceImpl;
import com.voo.airline.strategy.pricing.EconomyPriceStrategy;
import com.voo.airline.validator.BookingValidatorChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testa o BookingServiceImpl com todos os padrões de projeto mockados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl — testes de orquestração")
class BookingServiceImplTest {

    @Mock BookingRepository        bookingRepository;
    @Mock PassengerRepository      passengerRepository;
    @Mock PaymentRepository        paymentRepository;
    @Mock BookingValidatorChain    validatorChain;
    @Mock PriceStrategyFactory     priceStrategyFactory;
    @Mock PaymentFactory           paymentFactory;
    @Mock BookingMapper            bookingMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    BookingServiceImpl service;

    private CreateBookingRequest validRequest;
    private Passenger            passenger;
    private Booking              booking;
    private BookingResponse      response;

    @BeforeEach
    void setUp() {
        var passengerData = new PassengerDataRequest(
            "Maria Santos", "maria@email.com", "(81) 98888-7777",
            "11122233344", LocalDate.of(1992, 7, 10),
            DocType.CPF, "11122233344"
        );

        validRequest = new CreateBookingRequest(
            null, "VO5678", "REC", "BSB",
            LocalDate.now().plusDays(15), null,
            FlightType.ONEWAY, FlightClass.ECONOMY,
            "7C", "C1", "Boeing 737-800",
            "16:00", "15:30", PaymentMethod.CREDIT_CARD, passengerData
        );

        passenger = Passenger.of("Maria Santos", "maria@email.com",
            "(81) 98888-7777", "11122233344",
            LocalDate.of(1992, 7, 10), DocType.CPF, "11122233344");

        booking = Booking.create(
            "VOOTEST1", "VO5678", "REC", "BSB",
            LocalDate.now().plusDays(15), null,
            FlightType.ONEWAY, FlightClass.ECONOMY,
            "7C", "C1", "Boeing 737-800",
            "16:00", "15:30", new BigDecimal("350.00"), passenger
        );

        response = new BookingResponse(
            1L, "VOOTEST1", "VO5678", "REC", "BSB",
            LocalDate.now().plusDays(15), null,
            FlightType.ONEWAY, FlightClass.ECONOMY,
            "7C", "C1", "Boeing 737-800", "16:00", "15:30",
            new BigDecimal("350.00"), BookingStatus.CONFIRMED,
            null, booking.getCreatedAt()
        );
    }

    @Test
    @DisplayName("create() deve chamar validatorChain, strategy, factory e publisher")
    void createOrchestratesAllPatterns() {
        // Mocks
        doNothing().when(validatorChain).validateAll(any());
        when(priceStrategyFactory.getStrategy(FlightClass.ECONOMY))
            .thenReturn(new EconomyPriceStrategy());
        when(passengerRepository.findByCpf(anyString())).thenReturn(Optional.of(passenger));
        when(bookingRepository.existsByLocator(anyString())).thenReturn(false);
        when(bookingMapper.toEntity(any(), anyString(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(paymentFactory.create(any(), any(), any())).thenReturn(mock(com.voo.airline.entity.Payment.class));
        when(bookingMapper.toResponse(any())).thenReturn(response);

        BookingResponse result = service.create(validRequest);

        // Verifica que todos os colaboradores foram acionados
        verify(validatorChain, times(1)).validateAll(validRequest);
        verify(priceStrategyFactory, times(1)).getStrategy(FlightClass.ECONOMY);
        verify(paymentFactory, times(1)).create(any(), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any(BookingCreatedEvent.class));
        assertThat(result).isNotNull();
        assertThat(result.locator()).isEqualTo("VOOTEST1");
    }

    @Test
    @DisplayName("cancel() deve publicar BookingCancelledEvent")
    void cancelPublishesEvent() {
        when(bookingRepository.findByLocator("VOOTEST1")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(response);

        service.cancel("VOOTEST1");

        verify(eventPublisher, times(1))
            .publishEvent(any(com.voo.airline.observer.event.BookingCancelledEvent.class));
    }

    @Test
    @DisplayName("findByLocator() com localizador inexistente lança ResourceNotFoundException")
    void findByLocatorNotFound() {
        when(bookingRepository.findByLocator(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByLocator("VOOINEXISTENTE"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("cancel() usa método de negócio da entidade (encapsulamento)")
    void cancelUsesEntityBusinessMethod() {
        when(bookingRepository.findByLocator(anyString())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(response);

        service.cancel("VOOTEST1");

        // O status deve ter sido alterado pelo método booking.cancel()
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Strategy: preço deve ser calculado pela EconomyPriceStrategy")
    void priceCalculatedByStrategy() {
        var strategy = new EconomyPriceStrategy();

        doNothing().when(validatorChain).validateAll(any());
        when(priceStrategyFactory.getStrategy(FlightClass.ECONOMY)).thenReturn(strategy);
        when(passengerRepository.findByCpf(anyString())).thenReturn(Optional.of(passenger));
        when(bookingRepository.existsByLocator(anyString())).thenReturn(false);
        when(bookingMapper.toEntity(any(), anyString(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(paymentFactory.create(any(), any(), any())).thenReturn(mock(com.voo.airline.entity.Payment.class));
        when(bookingMapper.toResponse(any())).thenReturn(response);

        service.create(validRequest);

        // Captura o valor passado ao paymentFactory
        ArgumentCaptor<BigDecimal> priceCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(paymentFactory).create(any(), any(), priceCaptor.capture());

        // Economy ONEWAY 1 pax = R$ 350
        assertThat(priceCaptor.getValue())
            .isEqualByComparingTo(new BigDecimal("350.00"));
    }
}
