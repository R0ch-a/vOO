package com.voo.airline.validator;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * Valida que origem e destino são aeroportos diferentes.
 */
@Component
public class RouteValidator extends AbstractBookingValidator {

    @Override
    protected void doValidate(CreateBookingRequest request) {
        if (request.origin().equalsIgnoreCase(request.destination())) {
            throw new BusinessException(
                "Origem e destino não podem ser o mesmo aeroporto: '%s'."
                    .formatted(request.origin().toUpperCase()));
        }
    }

    @Override
    public String getRuleName() {
        return "ValidaçãoRota";
    }
}
