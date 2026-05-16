package com.voo.airline.repository;

import com.voo.airline.entity.Booking;
import com.voo.airline.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByLocator(String locator);

    boolean existsByLocator(String locator);

    // Listar reservas com dados do passageiro num JOIN só (evita N+1)
    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.passenger p
        ORDER BY b.createdAt DESC
    """)
    Page<Booking> findAllWithPassenger(Pageable pageable);

    // Busca por passageiro
    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.passenger p
        WHERE p.cpf = :cpf
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findByPassengerCpf(@Param("cpf") String cpf);

    // Busca por rota + data
    List<Booking> findByOriginAndDestinationAndDepDate(
        String origin, String destination, LocalDate depDate
    );

    // Busca por status
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    // Verificar assento ocupado
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.origin     = :origin
        AND   b.destination = :destination
        AND   b.depDate    = :depDate
        AND   b.seat       = :seat
        AND   b.status    <> 'CANCELLED'
    """)
    boolean isSeatTaken(
        @Param("origin")      String origin,
        @Param("destination") String destination,
        @Param("depDate")     LocalDate depDate,
        @Param("seat")        String seat
    );
}
