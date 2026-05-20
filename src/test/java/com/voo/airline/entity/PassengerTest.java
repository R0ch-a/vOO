package com.voo.airline.entity;

import com.voo.airline.enums.DocType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Passenger — testes de regras de negócio")
class PassengerTest {

    // ── of() — factory method ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Passenger.of()")
    class OfTests {

        @Test
        @DisplayName("deve criar passageiro com dados válidos")
        void createsPassengerSuccessfully() {
            Passenger p = Passenger.of(
                    "Ana Lima", "ana@email.com", "11999999999",
                    "12345678901", LocalDate.of(1990, 5, 15),
                    DocType.PASSPORT, "AB123456");

            assertThat(p.getName()).isEqualTo("Ana Lima");
            assertThat(p.getEmail()).isEqualTo("ana@email.com");
            assertThat(p.getCpf()).isEqualTo("12345678901");
            assertThat(p.getDocType()).isEqualTo(DocType.PASSPORT);
        }

        @Test
        @DisplayName("deve remover espaços extras do nome")
        void stripsNameWhitespace() {
            Passenger p = Passenger.of("  João Silva  ", null, null,
                    null, null, null, null);
            assertThat(p.getName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando nome é nulo")
        void throwsWhenNameIsNull() {
            assertThatThrownBy(() ->
                    Passenger.of(null, null, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do passageiro é obrigatório");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando nome é vazio")
        void throwsWhenNameIsBlank() {
            assertThatThrownBy(() ->
                    Passenger.of("   ", null, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do passageiro é obrigatório");
        }

        @Test
        @DisplayName("deve aceitar campos opcionais nulos")
        void acceptsNullOptionalFields() {
            Passenger p = Passenger.of("Carlos", null, null,
                    null, null, null, null);
            assertThat(p.getName()).isEqualTo("Carlos");
            assertThat(p.getEmail()).isNull();
            assertThat(p.getCpf()).isNull();
        }
    }

    // ── updateContactInfo() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("updateContactInfo()")
    class UpdateContactInfoTests {

        @Test
        @DisplayName("deve atualizar email e telefone")
        void updatesEmailAndPhone() {
            Passenger p = Passenger.of("Pedro Costa", "pedro@old.com", "11000000000",
                    null, null, null, null);

            p.updateContactInfo("pedro@new.com", "11999999999");

            assertThat(p.getEmail()).isEqualTo("pedro@new.com");
            assertThat(p.getPhone()).isEqualTo("11999999999");
        }

        @Test
        @DisplayName("deve aceitar null como novo contato")
        void acceptsNullContactUpdate() {
            Passenger p = Passenger.of("Maria", "maria@email.com", "11111111111",
                    null, null, null, null);

            p.updateContactInfo(null, null);

            assertThat(p.getEmail()).isNull();
            assertThat(p.getPhone()).isNull();
        }
    }

    // ── getBookings() — imutabilidade ─────────────────────────────────────────

    @Nested
    @DisplayName("getBookings() — lista imutável")
    class GetBookingsTests {

        @Test
        @DisplayName("deve retornar lista imutável")
        void returnsUnmodifiableList() {
            Passenger p = Passenger.of("Teste", "t@t.com", null,
                    null, null, null, null);

            assertThatThrownBy(() -> p.getBookings().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
