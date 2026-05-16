package com.voo.airline.dto.response;

import com.voo.airline.entity.Passenger;
import com.voo.airline.enums.DocType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PassengerResponse(
    Long          id,
    String        name,
    String        email,
    String        phone,
    String        cpf,
    LocalDate     birthDate,
    DocType       docType,
    String        docNumber,
    LocalDateTime createdAt
) {
    public static PassengerResponse from(Passenger p) {
        return new PassengerResponse(
            p.getId(),
            p.getName(),
            p.getEmail(),
            p.getPhone(),
            p.getCpf(),
            p.getBirthDate(),
            p.getDocType(),
            p.getDocNumber(),
            p.getCreatedAt()
        );
    }
}
