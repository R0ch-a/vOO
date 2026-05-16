package com.voo.airline.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.voo.airline.enums.DocType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade que representa um passageiro da vOO.
 *
 * Herança: estende AbstractEntity, herdando id e timestamps.
 * Encapsulamento: construtor protegido — objetos só são criados pelo
 * factory method estático of(), garantindo que um passageiro
 * sempre tenha nome válido. A lista de reservas é exposta apenas como
 * cópia imutável via getBookings().
 */
@Getter
@Entity
@Table(name = "passengers")
public class Passenger extends AbstractEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 20)
    private String cpf;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DocType docType;

    @Column(name = "doc_number", length = 60)
    private String docNumber;

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Booking> bookings = new ArrayList<>();

    /** Construtor protegido — uso exclusivo do JPA. */
    protected Passenger() {}

    /**
     * Factory method — única forma válida de criar um Passenger.
     * Garante invariante: nome não pode ser nulo ou vazio.
     */
    public static Passenger of(String name, String email, String phone,
                                String cpf, LocalDate birthDate,
                                DocType docType, String docNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do passageiro é obrigatório.");
        }
        Passenger p = new Passenger();
        p.name      = name.strip();
        p.email     = email;
        p.phone     = phone;
        p.cpf       = cpf;
        p.birthDate = birthDate;
        p.docType   = docType;
        p.docNumber = docNumber;
        return p;
    }

    /** Atualiza dados de contato preservando o id. */
    public void updateContactInfo(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    /** Retorna cópia imutável para proteger o estado interno. */
    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    @Override
    public String toString() {
        return "Passenger{id=%d, name='%s', cpf='%s'}".formatted(id, name, cpf);
    }
}
