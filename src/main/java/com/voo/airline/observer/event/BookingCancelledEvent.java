package com.voo.airline.observer.event;

import com.voo.airline.entity.Booking;
import org.springframework.context.ApplicationEvent;

/**
 * Evento disparado quando uma reserva é cancelada.
 */
public class BookingCancelledEvent extends ApplicationEvent {

    private final Booking booking;

    public BookingCancelledEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
