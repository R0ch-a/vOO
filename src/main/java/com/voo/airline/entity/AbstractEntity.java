package com.voo.airline.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe abstrata base para todas as entidades JPA da vOO.
 *
 * <p>Aplica os princípios de <b>Herança</b> e <b>Encapsulamento</b>:
 * concentra os campos comuns (id, timestamps) em um único lugar,
 * forçando que toda entidade concreta herde esse comportamento
 * sem duplicação de código.
 *
 * <p>Os campos são {@code protected} — acessíveis nas subclasses,
 * mas nunca expostos diretamente fora da hierarquia.
 */
@Getter
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    /**
     * Duas entidades são iguais se tiverem o mesmo id persistido.
     * Implementação correta de equals/hashCode para entidades JPA.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Indica se a entidade ainda não foi persistida. */
    public boolean isNew() {
        return id == null;
    }
}
