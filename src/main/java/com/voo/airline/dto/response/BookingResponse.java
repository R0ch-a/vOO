package com.voo.airline.dto.response;

import com.voo.airline.entity.Booking;
import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
    Long            id,
    String          locator,
    String          flightNum,
    String          origin,
    String          destination,
    LocalDate       depDate,
    LocalDate       retDate,
    FlightType      flightType,
    FlightClass     flightClass,
    String          seat,
    String          gate,
    String          aircraft,
    String          departure,
    String          boarding,
    BigDecimal      totalPrice,
    BookingStatus   status,
    PassengerResponse passenger,
    LocalDateTime   createdAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
            b.getId(),
            b.getLocator(),
            b.getFlightNum(),
            b.getOrigin(),
            b.getDestination(),
            b.getDepDate(),
            b.getRetDate(),
            b.getFlightType(),
            b.getFlightClass(),
            b.getSeat(),
            b.getGate(),
            b.getAircraft(),
            b.getDeparture(),
            b.getBoarding(),
            b.getTotalPrice(),
            b.getStatus(),
            b.getPassenger() != null ? PassengerResponse.from(b.getPassenger()) : null,
            b.getCreatedAt()
        );
    }
}
