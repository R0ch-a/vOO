package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.enums.FlightType;
import com.voo.airline.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * Valida as datas de voos de ida e volta.
 */
@Component
public class RoundTripDateValidator extends AbstractBookingValidator {

    @Override
    protected void doValidate(CreateBookingRequest request) {
        if (request.flightType() != FlightType.ROUNDTRIP) return;

        if (request.retDate() == null) {
            throw new BusinessException(
                "Data de volta é obrigatória para voos de ida e volta.");
        }
        if (!request.retDate().isAfter(request.depDate())) {
            throw new BusinessException(
                "Data de volta (%s) deve ser posterior à data de ida (%s)."
                    .formatted(request.retDate(), request.depDate()));
        }
    }

    @Override
    public String getRuleName() {
        return "ValidaçãoDataVolta";
    }
}
