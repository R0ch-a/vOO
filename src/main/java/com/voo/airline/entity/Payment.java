package com.voo.airline.entity;

import com.voo.airline.enums.PaymentMethod;
import com.voo.airline.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um pagamento associado a uma reserva.
 *
 * Herança: estende AbstractEntity.
 * Encapsulamento: status muda somente via markAsPaid() e markAsFailed().
 */
@Getter
@Entity
@Table(name = "payments")
public class Payment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_method_enum")
    private PaymentMethod method;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_status_enum")
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    protected Payment() {}

    /** Cria um Payment no estado PENDING, vinculado à booking. */
    public static Payment of(Booking booking, PaymentMethod method, BigDecimal amount) {
        Payment p  = new Payment();
        p.booking  = booking;
        p.method   = method;
        p.amount   = amount;
        p.status   = PaymentStatus.PENDING;
        return p;
    }

    public void markAsPaid() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    @Override
    public String toString() {
        return "Payment{id=%d, method=%s, amount=%s, status=%s}"
            .formatted(id, method, amount, status);
    }
}
