package com.voo.airline.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voo.airline.dto.request.CreateBookingRequest;
import com.voo.airline.dto.request.PassengerDataRequest;
import com.voo.airline.dto.response.ApiResponse;
import com.voo.airline.dto.response.BookingResponse;
import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import com.voo.airline.enums.PaymentMethod;
import com.voo.airline.exception.ResourceNotFoundException;
import com.voo.airline.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@DisplayName("BookingController — testes de integração da camada web")
class BookingControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;
    @MockBean  BookingService bookingService;

    private BookingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new BookingResponse(
                1L, "VOOTST01", "VO1001",
                "GIG", "LHR",
                LocalDate.now().plusDays(10), null,
                FlightType.ONEWAY, FlightClass.ECONOMY,
                "12A", "B5", "Boeing 737",
                "14:00", "13:30",
                BigDecimal.valueOf(320),
                BookingStatus.CONFIRMED, null, null);
    }

    // ── POST /api/bookings ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/bookings")
    class CreateEndpoint {

        private CreateBookingRequest validRequest() {
            PassengerDataRequest pdr = new PassengerDataRequest(
                    "Maria Souza", "maria@email.com", null,
                    "98765432100", null, null, null);
            return new CreateBookingRequest(
                    null, "VO1001", "GIG", "LHR",
                    LocalDate.now().plusDays(10), null,
                    FlightType.ONEWAY, FlightClass.ECONOMY,
                    "12A", "B5", "Boeing 737",
                    "14:00", "13:30",
                    PaymentMethod.CREDIT_CARD, pdr);
        }

        @Test
        @DisplayName("deve retornar 201 com body quando reserva é criada")
        void returns201OnSuccess() throws Exception {
            when(bookingService.create(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.locator").value("VOOTST01"))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("deve retornar 400 quando request é inválido")
        void returns400OnInvalidRequest() throws Exception {
            String invalidJson = """
                    {
                      "flightNum": "",
                      "origin": "GIG",
                      "destination": "LHR"
                    }
                    """;

            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/bookings/{locator} ───────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/bookings/{locator}")
    class FindByLocatorEndpoint {

        @Test
        @DisplayName("deve retornar 200 com reserva quando localizador existe")
        void returns200WhenFound() throws Exception {
            when(bookingService.findByLocator("VOOTST01")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/bookings/VOOTST01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.locator").value("VOOTST01"))
                    .andExpect(jsonPath("$.data.origin").value("GIG"))
                    .andExpect(jsonPath("$.data.destination").value("LHR"));
        }

        @Test
        @DisplayName("deve retornar 404 quando localizador não existe")
        void returns404WhenNotFound() throws Exception {
            when(bookingService.findByLocator("VOOXXXXX"))
                    .thenThrow(new ResourceNotFoundException("Reserva", "locator", "VOOXXXXX"));

            mockMvc.perform(get("/api/bookings/VOOXXXXX"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET /api/bookings ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/bookings")
    class FindAllEndpoint {

        @Test
        @DisplayName("deve retornar 200 com página de reservas")
        void returns200WithPage() throws Exception {
            var page = new PageImpl<>(List.of(sampleResponse),
                    PageRequest.of(0, 20), 1);
            when(bookingService.findAll(any())).thenReturn(page);

            mockMvc.perform(get("/api/bookings?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    // ── PATCH /api/bookings/{locator}/cancel ──────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/bookings/{locator}/cancel")
    class CancelEndpoint {

        @Test
        @DisplayName("deve retornar 200 ao cancelar reserva existente")
        void returns200OnCancel() throws Exception {
            BookingResponse cancelled = new BookingResponse(
                    1L, "VOOTST01", "VO1001",
                    "GIG", "LHR",
                    LocalDate.now().plusDays(10), null,
                    FlightType.ONEWAY, FlightClass.ECONOMY,
                    "12A", "B5", "Boeing 737",
                    "14:00", "13:30",
                    BigDecimal.valueOf(320),
                    BookingStatus.CANCELLED, null, null);
            when(bookingService.cancel("VOOTST01")).thenReturn(cancelled);

            mockMvc.perform(patch("/api/bookings/VOOTST01/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("deve retornar 404 ao cancelar localizador inexistente")
        void returns404WhenNotFound() throws Exception {
            when(bookingService.cancel("VOOXXXXX"))
                    .thenThrow(new ResourceNotFoundException("Reserva", "locator", "VOOXXXXX"));

            mockMvc.perform(patch("/api/bookings/VOOXXXXX/cancel"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PATCH /api/bookings/{locator}/complete ────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/bookings/{locator}/complete")
    class CompleteEndpoint {

        @Test
        @DisplayName("deve retornar 200 ao concluir reserva existente")
        void returns200OnComplete() throws Exception {
            BookingResponse completed = new BookingResponse(
                    1L, "VOOTST01", "VO1001",
                    "GIG", "LHR",
                    LocalDate.now().plusDays(10), null,
                    FlightType.ONEWAY, FlightClass.ECONOMY,
                    "12A", "B5", "Boeing 737",
                    "14:00", "13:30",
                    BigDecimal.valueOf(320),
                    BookingStatus.COMPLETED, null, null);
            when(bookingService.complete("VOOTST01")).thenReturn(completed);

            mockMvc.perform(patch("/api/bookings/VOOTST01/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("deve retornar 404 ao concluir localizador inexistente")
        void returns404WhenNotFound() throws Exception {
            when(bookingService.complete("VOOXXXXX"))
                    .thenThrow(new ResourceNotFoundException("Reserva", "locator", "VOOXXXXX"));

            mockMvc.perform(patch("/api/bookings/VOOXXXXX/complete"))
                    .andExpect(status().isNotFound());
        }
    }
}
