package com.voo.airline.service.impl;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.entity.Payment;
import com.voo.airline.exception.ResourceNotFoundException;
import com.voo.airline.factory.PaymentFactory;
import com.voo.airline.factory.PriceStrategyFactory;
import com.voo.airline.mapper.BookingMapper;
import com.voo.airline.observer.event.BookingCancelledEvent;
import com.voo.airline.observer.event.BookingCreatedEvent;
import com.voo.airline.repository.BookingRepository;
import com.voo.airline.repository.PassengerRepository;
import com.voo.airline.repository.PaymentRepository;
import com.voo.airline.service.BookingService;
import com.voo.airline.strategy.pricing.PriceStrategy;
import com.voo.airline.validator.BookingValidatorChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository        bookingRepository;
    private final PassengerRepository      passengerRepository;
    private final PaymentRepository        paymentRepository;
    private final BookingValidatorChain    validatorChain;
    private final PriceStrategyFactory     priceStrategyFactory;
    private final PaymentFactory           paymentFactory;
    private final BookingMapper            bookingMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ------------------------------------------------------------------ //
    //  CREATE
    // ------------------------------------------------------------------ //
    @Override
    @Transactional
    public BookingResponse create(CreateBookingRequest req) {
        log.info("Criando reserva: {} -> {} em {}", req.origin(), req.destination(), req.depDate());

        validatorChain.validateAll(req);

        Passenger passenger = resolvePassenger(req.passengerData());

        PriceStrategy strategy   = priceStrategyFactory.getStrategy(req.flightClass());
        BigDecimal    totalPrice = strategy.calculate(req.flightType(), 1);
        log.debug("Estratégia aplicada: {} — total: R$ {}", strategy.getStrategyName(), totalPrice);

        String locator = generateLocator(req.locator());

        Booking booking = bookingMapper.toEntity(req, locator, totalPrice, passenger);
        booking = bookingRepository.save(booking);

        Payment payment = paymentFactory.create(booking, req.payMethod(), totalPrice);
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new BookingCreatedEvent(this, booking));

        log.info("Reserva {} criada com sucesso — R$ {}", locator, totalPrice);
        return bookingMapper.toResponse(booking);
    }

    // ------------------------------------------------------------------ //
    //  READ
    // ------------------------------------------------------------------ //
    @Override
    @Transactional(readOnly = true)
    public BookingResponse findByLocator(String locator) {
        Booking booking = bookingRepository.findByLocator(locator.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva", "locator", locator));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> findAll(Pageable pageable) {
        return bookingRepository.findAllWithPassenger(pageable)
            .map(bookingMapper::toResponse);
    }

    // ------------------------------------------------------------------ //
    //  CANCEL
    // ------------------------------------------------------------------ //
    @Override
    @Transactional
    public BookingResponse cancel(String locator) {
        Booking booking = bookingRepository.findByLocator(locator.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva", "locator", locator));

        booking.cancel();
        bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingCancelledEvent(this, booking));

        log.info("Reserva {} cancelada.", locator);
        return bookingMapper.toResponse(booking);
    }

    // ------------------------------------------------------------------ //
    //  COMPLETE
    // ------------------------------------------------------------------ //
    @Override
    @Transactional
    public BookingResponse complete(String locator) {
        Booking booking = bookingRepository.findByLocator(locator.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva", "locator", locator));

        booking.complete();
        bookingRepository.save(booking);

        log.info("Reserva {} concluída via check-in.", locator);
        return bookingMapper.toResponse(booking);
    }

    // ------------------------------------------------------------------ //
    //  HELPERS PRIVADOS
    // ------------------------------------------------------------------ //
    private Passenger resolvePassenger(PassengerDataRequest data) {
        if (data.cpf() != null && !data.cpf().isBlank()) {
            String cpfLimpo = data.cpf().replaceAll("[^0-9]", "");
            return passengerRepository.findByCpf(cpfLimpo)
                .orElseGet(() -> passengerRepository.save(
                    Passenger.of(data.name(), data.email(), data.phone(),
                        cpfLimpo, data.birthDate(), data.docType(), data.docNumber())
                ));
        }
        return passengerRepository.save(
            Passenger.of(data.name(), data.email(), data.phone(),
                null, data.birthDate(), data.docType(), data.docNumber())
        );
    }

    private String generateLocator(String requested) {
        if (requested != null && !requested.isBlank()) {
            String upper = requested.toUpperCase();
            if (!bookingRepository.existsByLocator(upper)) return upper;
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd   = new Random();
        String locator;
        do {
            StringBuilder sb = new StringBuilder("VOO");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            locator = sb.toString();
        } while (bookingRepository.existsByLocator(locator));
        return locator;
    }
}
