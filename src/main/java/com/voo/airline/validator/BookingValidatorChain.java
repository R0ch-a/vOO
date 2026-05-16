package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Monta e executa a cadeia de validadores de reserva.
 *
 * <p>Combina <b>Chain of Responsibility</b> com <b>Template Method</b>:
 * cada {@link AbstractBookingValidator} cuida de uma regra específica e
 * passa para o próximo automaticamente. O cliente (Service) chama apenas
 * {@link #validateAll}, sem conhecer os validadores individuais.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingValidatorChain {

    private final RouteValidator            routeValidator;
    private final RoundTripDateValidator    roundTripDateValidator;
    private final SeatAvailabilityValidator seatAvailabilityValidator;

    /**
     * Executa toda a cadeia de validação na ordem:
     * Rota → Datas → Assento
     */
    public void validateAll(CreateBookingRequest request) {
        log.debug("Iniciando cadeia de validação para reserva {}->{} em {}",
            request.origin(), request.destination(), request.depDate());

        // Monta a cadeia
        routeValidator
            .setNext(roundTripDateValidator)
            .setNext(seatAvailabilityValidator);

        // Dispara a cadeia — cada validador chama o próximo internamente
        routeValidator.validate(request);

        log.debug("Cadeia de validação concluída com sucesso.");
    }
}
