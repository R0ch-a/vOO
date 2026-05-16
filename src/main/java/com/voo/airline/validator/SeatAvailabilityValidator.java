package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.exception.BusinessException;
import com.voo.airline.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Valida que o assento não está ocupado para o voo solicitado.
 */
@Component
@RequiredArgsConstructor
public class SeatAvailabilityValidator extends AbstractBookingValidator {

    private final BookingRepository bookingRepository;

    @Override
    protected void doValidate(CreateBookingRequest request) {
        if (request.seat() == null || request.seat().isBlank()) return;

        boolean taken = bookingRepository.isSeatTaken(
            request.origin(),
            request.destination(),
            request.depDate(),
            request.seat()
        );

        if (taken) {
            throw new BusinessException(
                "O assento '%s' já está ocupado para o voo %s->%s em %s."
                    .formatted(request.seat(), request.origin(),
                        request.destination(), request.depDate()));
        }
    }

    @Override
    public String getRuleName() {
        return "ValidaçãoDisponibilidadeAssento";
    }
}
