-- ============================================================
--  vOO Airline — V1: Criação das tabelas iniciais
-- ============================================================

-- ENUM types
CREATE TYPE flight_type_enum  AS ENUM ('ONEWAY', 'ROUNDTRIP');
CREATE TYPE flight_class_enum AS ENUM ('ECONOMY', 'PREMIUM_ECONOMY', 'EXECUTIVE');
CREATE TYPE booking_status_enum AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'PAID', 'REFUNDED', 'FAILED');
CREATE TYPE payment_method_enum AS ENUM ('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BOLETO');
CREATE TYPE doc_type_enum AS ENUM ('CPF', 'RG', 'PASSPORT');

-- ---- PASSENGERS ----
CREATE TABLE passengers (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(30),
    cpf         VARCHAR(20),
    birth_date  DATE,
    doc_type    doc_type_enum,
    doc_number  VARCHAR(60),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_passengers_cpf   ON passengers(cpf);
CREATE INDEX idx_passengers_email ON passengers(email);

-- ---- BOOKINGS ----
CREATE TABLE bookings (
    id           BIGSERIAL           PRIMARY KEY,
    locator      VARCHAR(20)         NOT NULL UNIQUE,
    flight_num   VARCHAR(20)         NOT NULL,
    origin       VARCHAR(10)         NOT NULL,
    destination  VARCHAR(10)         NOT NULL,
    dep_date     DATE                NOT NULL,
    ret_date     DATE,
    flight_type  flight_type_enum    NOT NULL DEFAULT 'ONEWAY',
    flight_class flight_class_enum   NOT NULL,
    seat         VARCHAR(10),
    gate         VARCHAR(10),
    aircraft     VARCHAR(100),
    departure    VARCHAR(10),
    boarding     VARCHAR(10),
    total_price  NUMERIC(10, 2)      NOT NULL DEFAULT 0,
    status       booking_status_enum NOT NULL DEFAULT 'CONFIRMED',
    passenger_id BIGINT              REFERENCES passengers(id) ON DELETE SET NULL,
    created_at   TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bookings_locator     ON bookings(locator);
CREATE INDEX idx_bookings_passenger   ON bookings(passenger_id);
CREATE INDEX idx_bookings_origin_dest ON bookings(origin, destination);
CREATE INDEX idx_bookings_dep_date    ON bookings(dep_date);

-- ---- PAYMENTS ----
CREATE TABLE payments (
    id          BIGSERIAL            PRIMARY KEY,
    booking_id  BIGINT               NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    method      payment_method_enum  NOT NULL,
    amount      NUMERIC(10, 2)       NOT NULL,
    status      payment_status_enum  NOT NULL DEFAULT 'PENDING',
    paid_at     TIMESTAMP,
    created_at  TIMESTAMP            NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP            NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_booking ON payments(booking_id);
