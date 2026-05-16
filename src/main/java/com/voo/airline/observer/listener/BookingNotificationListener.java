package com.voo.airline.observer.listener;

import com.voo.airline.observer.event.BookingCancelledEvent;
import com.voo.airline.observer.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que reage a eventos de reserva — implementa o papel de
 * <b>Observer</b> no padrão Observer do Spring.
 *
 * <p>Este componente simula o envio de notificações (e-mail, SMS).
 * Em produção, aqui ficaria a chamada ao serviço de e-mail (SendGrid,
 * SES, etc.) sem que o BookingService precise saber disso.
 *
 * <p>O {@code @Async} garante que a notificação não bloqueie a resposta
 * HTTP ao usuário — execução em thread separada.
 */
@Slf4j
@Component
public class BookingNotificationListener {

    /**
     * Disparado quando uma reserva é criada.
     * Simula envio de e-mail de confirmação ao passageiro.
     */
    @Async
    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        var booking   = event.getBooking();
        var passenger = booking.getPassenger();

        log.info("[OBSERVER] Reserva criada — localizador: {} | passageiro: {} | rota: {}->{}",
            booking.getLocator(),
            passenger != null ? passenger.getName() : "N/A",
            booking.getOrigin(),
            booking.getDestination());

        // Em produção: emailService.sendConfirmation(passenger.getEmail(), booking);
        log.info("[OBSERVER] E-mail de confirmação enviado para: {}",
            passenger != null ? passenger.getEmail() : "sem e-mail");
    }

    /**
     * Disparado quando uma reserva é cancelada.
     * Simula envio de e-mail de cancelamento.
     */
    @Async
    @EventListener
    public void onBookingCancelled(BookingCancelledEvent event) {
        var booking   = event.getBooking();
        var passenger = booking.getPassenger();

        log.info("[OBSERVER] Reserva cancelada — localizador: {} | passageiro: {}",
            booking.getLocator(),
            passenger != null ? passenger.getName() : "N/A");

        // Em produção: emailService.sendCancellation(passenger.getEmail(), booking);
        log.info("[OBSERVER] E-mail de cancelamento enviado para: {}",
            passenger != null ? passenger.getEmail() : "sem e-mail");
    }
}
