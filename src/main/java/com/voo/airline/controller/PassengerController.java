package com.voo.airline.controller;

import com.voo.airline.dto.response.ApiResponse;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.dto.response.PassengerResponse;
import com.voo.airline.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
@Tag(name = "Passageiros", description = "Endpoints para consulta de passageiros")
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar passageiro por ID")
    public ResponseEntity<ApiResponse<PassengerResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(passengerService.findById(id)));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar passageiro por CPF")
    public ResponseEntity<ApiResponse<PassengerResponse>> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(ApiResponse.ok(passengerService.findByCpf(cpf)));
    }

    @GetMapping("/cpf/{cpf}/bookings")
    @Operation(summary = "Listar reservas de um passageiro pelo CPF")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> findBookingsByCpf(
        @PathVariable String cpf
    ) {
        return ResponseEntity.ok(ApiResponse.ok(passengerService.findBookingsByCpf(cpf)));
    }
}
