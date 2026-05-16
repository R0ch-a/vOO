package com.voo.airline.dto.request;

import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import com.voo.airline.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateBookingRequest(

    // Localizador gerado pelo frontend (ou gerado pelo backend se null)
    String locator,

    @NotBlank(message = "Número do voo é obrigatório")
    String flightNum,

    @NotBlank(message = "Origem é obrigatória")
    @Size(min = 2, max = 10)
    String origin,

    @NotBlank(message = "Destino é obrigatório")
    @Size(min = 2, max = 10)
    String destination,

    @NotNull(message = "Data de ida é obrigatória")
    @FutureOrPresent(message = "Data de ida não pode ser no passado")
    LocalDate depDate,

    LocalDate retDate,

    FlightType flightType,

    @NotNull(message = "Classe do voo é obrigatória")
    FlightClass flightClass,

    String seat,

    String gate,

    String aircraft,

    String departure,

    String boarding,

    @NotNull(message = "Método de pagamento é obrigatório")
    PaymentMethod payMethod,

    // Dados do passageiro
    @Valid
    @NotNull(message = "Dados do passageiro são obrigatórios")
    PassengerDataRequest passengerData
) {}
