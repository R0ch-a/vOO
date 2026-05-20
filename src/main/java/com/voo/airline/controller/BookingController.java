package com.voo.airline.controller;

import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.response.ApiResponse;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Endpoints para gerenciamento de reservas de voo")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Criar uma nova reserva")
    public ResponseEntity<ApiResponse<BookingResponse>> create(
        @Valid @RequestBody CreateBookingRequest request
    ) {
        BookingResponse booking = bookingService.create(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Reserva criada com sucesso!", booking));
    }

    @GetMapping("/{locator}")
    @Operation(summary = "Buscar reserva pelo localizador")
    public ResponseEntity<ApiResponse<BookingResponse>> findByLocator(
        @PathVariable String locator
    ) {
        return ResponseEntity.ok(
            ApiResponse.ok(bookingService.findByLocator(locator))
        );
    }

    @GetMapping
    @Operation(summary = "Listar todas as reservas (paginado)")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> findAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(
            ApiResponse.ok(bookingService.findAll(pageable))
        );
    }

    @PatchMapping("/{locator}/cancel")
    @Operation(summary = "Cancelar uma reserva")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(
        @PathVariable String locator
    ) {
        return ResponseEntity.ok(
            ApiResponse.ok("Reserva cancelada.", bookingService.cancel(locator))
        );
    }

    @PatchMapping("/{locator}/complete")
    @Operation(summary = "Concluir uma reserva após check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> complete(
        @PathVariable String locator
    ) {
        return ResponseEntity.ok(
            ApiResponse.ok("Check-in concluído.", bookingService.complete(locator))
        );
    }
}
