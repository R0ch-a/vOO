package com.voo.airline.mapper;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.entity.Booking;
import com.voo.airline.entity.Passenger;
import com.voo.airline.enums.FlightType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper responsável por converter entre a entidade {@link Booking}
 * e seus DTOs de request/response.
 *
 * <p><b>Separação de responsabilidades:</b> o Service não precisa saber
 * como montar um DTO — delega ao Mapper. Isso facilita testes unitários
 * independentes e evolução do contrato da API sem impactar o domínio.
 */
@Component
public class BookingMapper {

    /**
     * Cria uma entidade {@link Booking} a partir do request e dos dados
     * complementares (localizador, preço, passageiro).
     */
    public Booking toEntity(CreateBookingRequest req, String locator,
                             BigDecimal totalPrice, Passenger passenger) {
        return Booking.create(
            locator,
            req.flightNum(),
            req.origin(),
            req.destination(),
            req.depDate(),
            req.retDate(),
            req.flightType() != null ? req.flightType() : FlightType.ONEWAY,
            req.flightClass(),
            req.seat(),
            req.gate(),
            req.aircraft(),
            req.departure(),
            req.boarding(),
            totalPrice,
            passenger
        );
    }

    /**
     * Converte uma entidade {@link Booking} para o DTO de resposta.
     */
    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.from(booking);
    }
}
