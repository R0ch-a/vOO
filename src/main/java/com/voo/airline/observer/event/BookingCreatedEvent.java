package com.voo.airline.observer.event;

import com.voo.airline.entity.Booking;
import org.springframework.context.ApplicationEvent;

/**
 * Evento disparado quando uma reserva é criada com sucesso.
 *
 * <p><b>Padrão Observer:</b> o {@link BookingCreatedEvent} é publicado pelo
 * Service no momento em que a reserva é persistida. Qualquer listener
 * registrado no contexto do Spring reage automaticamente, sem que o
 * Service precise conhecer quem são os observadores.
 *
 * <p>Isso respeita o princípio SRP: o Service cria reservas, os Listeners
 * tratam as consequências (notificação, log de auditoria, etc.).
 */
public class BookingCreatedEvent extends ApplicationEvent {

    private final Booking booking;

    public BookingCreatedEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
