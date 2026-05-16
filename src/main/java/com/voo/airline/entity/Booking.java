package com.voo.airline.entity;

import com.voo.airline.enums.BookingStatus;
import com.voo.airline.enums.FlightClass;
import com.voo.airline.enums.FlightType;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade que representa uma reserva de voo da vOO.
 *
 * Herança: estende AbstractEntity.
 * Encapsulamento: status só muda via métodos de negócio (confirm, cancel, complete),
 * nunca por setter direto — mantém invariantes do domínio.
 */
@Getter
@Entity
@Table(name = "bookings")
public class Booking extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String locator;

    @Column(name = "flight_num", nullable = false, length = 20)
    private String flightNum;

    @Column(nullable = false, length = 10)
    private String origin;

    @Column(nullable = false, length = 10)
    private String destination;

    @Column(name = "dep_date", nullable = false)
    private LocalDate depDate;

    @Column(name = "ret_date")
    private LocalDate retDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_type", nullable = false, columnDefinition = "flight_type_enum")
    private FlightType flightType;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_class", nullable = false, columnDefinition = "flight_class_enum")
    private FlightClass flightClass;

    @Column(length = 10)
    private String seat;

    @Column(length = 10)
    private String gate;

    @Column(length = 100)
    private String aircraft;

    @Column(length = 10)
    private String departure;

    @Column(length = 10)
    private String boarding;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "booking_status_enum")
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Payment> payments = new ArrayList<>();

    /** Construtor protegido — uso exclusivo do JPA. */
    protected Booking() {}

    /**
     * Factory method que cria uma Booking já no status CONFIRMED.
     * Todos os campos obrigatórios são verificados aqui.
     */
    public static Booking create(String locator, String flightNum,
                                  String origin, String destination,
                                  LocalDate depDate, LocalDate retDate,
                                  FlightType flightType, FlightClass flightClass,
                                  String seat, String gate, String aircraft,
                                  String departure, String boarding,
                                  BigDecimal totalPrice, Passenger passenger) {
        Booking b = new Booking();
        b.locator      = locator;
        b.flightNum    = flightNum;
        b.origin       = origin.toUpperCase();
        b.destination  = destination.toUpperCase();
        b.depDate      = depDate;
        b.retDate      = retDate;
        b.flightType   = flightType != null ? flightType : FlightType.ONEWAY;
        b.flightClass  = flightClass;
        b.seat         = seat;
        b.gate         = gate;
        b.aircraft     = aircraft;
        b.departure    = departure;
        b.boarding     = boarding;
        b.totalPrice   = totalPrice;
        b.status       = BookingStatus.CONFIRMED;
        b.passenger    = passenger;
        return b;
    }

    // ---- Métodos de negócio que encapsulam transições de estado ----

    /** Cancela a reserva. Lança exceção se já estiver cancelada. */
    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                "Reserva %s já está cancelada.".formatted(locator));
        }
        this.status = BookingStatus.CANCELLED;
    }

    /** Conclui a reserva após o voo realizado. */
    public void complete() {
        if (this.status != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                "Somente reservas confirmadas podem ser concluídas.");
        }
        this.status = BookingStatus.COMPLETED;
    }

    public boolean isCancellable() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }

    public boolean isRoundTrip() {
        return flightType == FlightType.ROUNDTRIP;
    }

    /** Retorna cópia imutável dos pagamentos. */
    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    @Override
    public String toString() {
        return "Booking{locator='%s', %s->%s, %s}".formatted(
            locator, origin, destination, status);
    }
}
