package com.voo.airline.dto.request;

import com.voo.airline.enums.DocType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PassengerDataRequest(

    @NotBlank(message = "Nome do passageiro é obrigatório")
    @Size(min = 2, max = 255)
    String name,

    @Email(message = "E-mail inválido")
    String email,

    String phone,

    String cpf,

    LocalDate birthDate,

    DocType docType,

    String docNumber
) {}
