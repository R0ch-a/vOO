-- ============================================================
--  SkyPass — Schema ERD para importação em ferramenta visual
--  Compatível com: dbdiagram.io (DBML), drawio, Lucidchart,
--                  pgAdmin, DBeaver, DataGrip
--
--  Instruções por ferramenta:
--  • dbdiagram.io  → usar o bloco DBML abaixo (Seção 1)
--  • drawio        → File > Import > SQL (usar Seção 2 - DDL)
--  • DBeaver       → SQL Editor > executar Seção 2 em BD local
--  • pgAdmin       → Query Tool > executar Seção 2
-- ============================================================


-- ============================================================
--  SEÇÃO 1 — DBML  (cole em https://dbdiagram.io)
-- ============================================================
/*
Table usuarios {
  id bigint [pk, increment, note: "PK — identificador único"]
  email varchar [unique, not null, note: "UK — login do usuário"]
  senha_hash varchar [not null, note: "BCrypt fator 12"]
  nome_completo varchar [not null]
  telefone varchar
  tipo_usuario tipo_usuario_enum [not null, note: "PASSAGEIRO | ADMINISTRADOR"]
  ativo boolean [default: true]
  criado_em timestamptz [default: `now()`]
}

Table passageiros {
  id bigint [pk, ref: - usuarios.id, note: "PK e FK — herança vertical"]
  cpf varchar(14) [unique, not null, note: "UK — validado algoritmicamente"]
  data_nascimento date [not null]
  tipo_documento tipo_documento_enum [not null, note: "CPF | RG | PASSAPORTE"]
  numero_documento varchar [not null]
}

Table administradores {
  id bigint [pk, ref: - usuarios.id, note: "PK e FK — herança vertical"]
  matricula varchar [unique, not null, note: "UK"]
  departamento varchar
}

Table voos {
  id bigint [pk, increment]
  numero_voo varchar(7) [unique, not null, note: "UK — formato ICAO: 2-3 letras + 4 dígitos"]
  origem_iata char(3) [not null, note: "Código IATA do aeroporto de origem"]
  destino_iata char(3) [not null, note: "Código IATA do aeroporto de destino"]
  partida_em timestamptz [not null]
  chegada_em timestamptz [not null]
  aeronave varchar
  capacidade_total integer [not null]
  status_voo status_voo_enum [not null, default: "SCHEDULED", note: "SCHEDULED|BOARDING|DEPARTED|LANDED|CANCELLED|DELAYED"]
}

Table assentos {
  id bigint [pk, increment]
  voo_id bigint [not null, ref: > voos.id, note: "FK ON DELETE RESTRICT"]
  codigo varchar(4) [not null, note: "ex: 12A — unique por voo"]
  classe classe_enum [not null, note: "ECONOMY | PREMIUM_ECONOMY | BUSINESS"]
  status_assento status_assento_enum [not null, default: "AVAILABLE", note: "AVAILABLE | BLOCKED | OCCUPIED"]
  bloqueado_ate timestamptz [note: "Preenchido durante fluxo de pagamento (15 min)"]

  indexes {
    (voo_id, codigo) [unique, name: "uq_assento_voo_codigo"]
  }
}

Table tarifas {
  id bigint [pk, increment]
  voo_id bigint [not null, ref: > voos.id, note: "FK ON DELETE RESTRICT"]
  classe classe_enum [not null]
  tipo_tarifa tipo_tarifa_enum [not null, note: "PROMOTIONAL | FLEXIBLE | BUSINESS"]
  preco_base numeric(10,2) [not null]
  franquia_bagagem_kg integer [not null, default: 23]
  permite_alteracao boolean [not null, default: false]
  permite_reembolso boolean [not null, default: false]
}

Table reservas {
  id bigint [pk, increment]
  localizador varchar(9) [unique, not null, note: "UK — formato VOO + 6 alfanuméricos"]
  passageiro_id bigint [not null, ref: > passageiros.id, note: "FK ON DELETE RESTRICT"]
  voo_id bigint [not null, ref: > voos.id, note: "FK ON DELETE RESTRICT"]
  assento_id bigint [not null, ref: > assentos.id, note: "FK ON DELETE RESTRICT"]
  tarifa_id bigint [not null, ref: > tarifas.id, note: "FK ON DELETE RESTRICT"]
  status_reserva status_reserva_enum [not null, default: "PENDING", note: "PENDING|CONFIRMED|CHECKED_IN|COMPLETED|CANCELLED"]
  criado_em timestamptz [default: `now()`]
  atualizado_em timestamptz [default: `now()`]
}

Table pagamentos {
  id bigint [pk, increment]
  reserva_id bigint [not null, ref: > reservas.id, note: "FK ON DELETE RESTRICT"]
  metodo_pagamento metodo_pagamento_enum [not null, note: "CREDIT_CARD | DEBIT_CARD | PIX | BOLETO"]
  valor numeric(10,2) [not null]
  status_pagamento status_pagamento_enum [not null, note: "PENDING|APPROVED|REJECTED|REFUNDED"]
  referencia_gateway varchar [note: "Token retornado pelo gateway — nunca dados do cartão"]
  processado_em timestamptz
}

Table bilhetes {
  id bigint [pk, increment]
  reserva_id bigint [unique, not null, ref: - reservas.id, note: "FK UK — 1 bilhete por reserva"]
  numero_bilhete varchar [unique, not null, note: "UK"]
  url_pdf varchar [note: "Caminho do PDF gerado"]
  qr_code varchar [note: "Conteúdo codificado no QR Code"]
  emitido_em timestamptz [default: `now()`]
}

Table checkins {
  id bigint [pk, increment]
  reserva_id bigint [unique, not null, ref: - reservas.id, note: "FK UK — 1 check-in por reserva"]
  realizado_em timestamptz [default: `now()`]
  documento_verificado varchar [not null]
}

Table cartoes_embarque {
  id bigint [pk, increment]
  checkin_id bigint [unique, not null, ref: - checkins.id, note: "FK UK — 1 cartão por check-in"]
  portao varchar
  limite_embarque timestamptz
  url_pdf varchar
  qr_code varchar
  emitido_em timestamptz [default: `now()`]
}

Table bagagens {
  id bigint [pk, increment]
  reserva_id bigint [not null, ref: > reservas.id, note: "FK ON DELETE RESTRICT"]
  peso_estimado_kg integer [not null]
  valor_cobrado numeric(8,2) [not null, default: 0]
  status_bagagem status_bagagem_enum [not null, default: "ADDED", note: "ADDED | CHECKED_IN | DELIVERED"]
}

Table contas_milhas {
  id bigint [pk, increment]
  passageiro_id bigint [unique, not null, ref: - passageiros.id, note: "FK UK — 1 conta por passageiro"]
  saldo_atual integer [not null, default: 0, note: "CHECK saldo_atual >= 0"]
  atualizado_em timestamptz [default: `now()`]
}

Table transacoes_milhas {
  id bigint [pk, increment]
  conta_milhas_id bigint [not null, ref: > contas_milhas.id, note: "FK ON DELETE RESTRICT"]
  reserva_id bigint [ref: > reservas.id, note: "FK nullable — null em estornos manuais"]
  tipo_transacao tipo_transacao_enum [not null, note: "CREDIT | DEBIT | EXPIRATION"]
  quantidade integer [not null]
  expira_em timestamptz [note: "Preenchido em créditos — validade 2 anos"]
  criado_em timestamptz [default: `now()`]
}

Table notificacoes {
  id bigint [pk, increment]
  passageiro_id bigint [not null, ref: > passageiros.id, note: "FK ON DELETE RESTRICT"]
  voo_id bigint [ref: > voos.id, note: "FK nullable"]
  tipo_notificacao tipo_notificacao_enum [not null, note: "BOOKING_CONFIRMED|CHECKIN_OPEN|FLIGHT_CHANGE|CANCELLATION|MILES_CREDITED"]
  mensagem text [not null]
  enviada boolean [not null, default: false]
  criado_em timestamptz [default: `now()`]
}

Enum tipo_usuario_enum     { PASSAGEIRO ADMINISTRADOR }
Enum tipo_documento_enum   { CPF RG PASSAPORTE }
Enum classe_enum           { ECONOMY PREMIUM_ECONOMY BUSINESS }
Enum tipo_tarifa_enum      { PROMOTIONAL FLEXIBLE BUSINESS }
Enum status_voo_enum       { SCHEDULED BOARDING DEPARTED LANDED CANCELLED DELAYED }
Enum status_assento_enum   { AVAILABLE BLOCKED OCCUPIED }
Enum status_reserva_enum   { PENDING CONFIRMED CHECKED_IN COMPLETED CANCELLED }
Enum metodo_pagamento_enum { CREDIT_CARD DEBIT_CARD PIX BOLETO }
Enum status_pagamento_enum { PENDING APPROVED REJECTED REFUNDED }
Enum status_bagagem_enum   { ADDED CHECKED_IN DELIVERED }
Enum tipo_transacao_enum   { CREDIT DEBIT EXPIRATION }
Enum tipo_notificacao_enum { BOOKING_CONFIRMED CHECKIN_OPEN FLIGHT_CHANGE CANCELLATION MILES_CREDITED }
*/


-- ============================================================
--  SEÇÃO 2 — DDL PostgreSQL 16
--  (use no drawio, DBeaver, pgAdmin, DataGrip, etc.)
-- ============================================================

-- ENUMs nativos do PostgreSQL
CREATE TYPE tipo_usuario_enum     AS ENUM ('PASSAGEIRO', 'ADMINISTRADOR');
CREATE TYPE tipo_documento_enum   AS ENUM ('CPF', 'RG', 'PASSAPORTE');
CREATE TYPE classe_enum           AS ENUM ('ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS');
CREATE TYPE tipo_tarifa_enum      AS ENUM ('PROMOTIONAL', 'FLEXIBLE', 'BUSINESS');
CREATE TYPE status_voo_enum       AS ENUM ('SCHEDULED', 'BOARDING', 'DEPARTED', 'LANDED', 'CANCELLED', 'DELAYED');
CREATE TYPE status_assento_enum   AS ENUM ('AVAILABLE', 'BLOCKED', 'OCCUPIED');
CREATE TYPE status_reserva_enum   AS ENUM ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED');
CREATE TYPE metodo_pagamento_enum AS ENUM ('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BOLETO');
CREATE TYPE status_pagamento_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED');
CREATE TYPE status_bagagem_enum   AS ENUM ('ADDED', 'CHECKED_IN', 'DELIVERED');
CREATE TYPE tipo_transacao_enum   AS ENUM ('CREDIT', 'DEBIT', 'EXPIRATION');
CREATE TYPE tipo_notificacao_enum AS ENUM ('BOOKING_CONFIRMED', 'CHECKIN_OPEN', 'FLIGHT_CHANGE', 'CANCELLATION', 'MILES_CREDITED');

-- ── Tabela raiz da hierarquia de usuários ──
CREATE TABLE usuarios (
    id            BIGSERIAL     PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    senha_hash    VARCHAR(255)  NOT NULL,
    nome_completo VARCHAR(255)  NOT NULL,
    telefone      VARCHAR(20),
    tipo_usuario  tipo_usuario_enum NOT NULL,
    ativo         BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ── Herança vertical: passageiros ──
CREATE TABLE passageiros (
    id               BIGINT        PRIMARY KEY REFERENCES usuarios(id) ON DELETE RESTRICT,
    cpf              VARCHAR(14)   NOT NULL UNIQUE,
    data_nascimento  DATE          NOT NULL,
    tipo_documento   tipo_documento_enum NOT NULL,
    numero_documento VARCHAR(50)   NOT NULL
);

-- ── Herança vertical: administradores ──
CREATE TABLE administradores (
    id           BIGINT       PRIMARY KEY REFERENCES usuarios(id) ON DELETE RESTRICT,
    matricula    VARCHAR(50)  NOT NULL UNIQUE,
    departamento VARCHAR(100)
);

-- ── Voos ──
CREATE TABLE voos (
    id               BIGSERIAL         PRIMARY KEY,
    numero_voo       VARCHAR(7)        NOT NULL UNIQUE,
    origem_iata      CHAR(3)           NOT NULL,
    destino_iata     CHAR(3)           NOT NULL,
    partida_em       TIMESTAMPTZ       NOT NULL,
    chegada_em       TIMESTAMPTZ       NOT NULL,
    aeronave         VARCHAR(100),
    capacidade_total INTEGER           NOT NULL CHECK (capacidade_total > 0),
    status_voo       status_voo_enum   NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT chk_datas CHECK (chegada_em > partida_em)
);

-- ── Assentos ──
CREATE TABLE assentos (
    id              BIGSERIAL           PRIMARY KEY,
    voo_id          BIGINT              NOT NULL REFERENCES voos(id) ON DELETE RESTRICT,
    codigo          VARCHAR(4)          NOT NULL,
    classe          classe_enum         NOT NULL,
    status_assento  status_assento_enum NOT NULL DEFAULT 'AVAILABLE',
    bloqueado_ate   TIMESTAMPTZ,
    CONSTRAINT uq_assento_voo_codigo UNIQUE (voo_id, codigo)
);

-- ── Tarifas ──
CREATE TABLE tarifas (
    id                  BIGSERIAL          PRIMARY KEY,
    voo_id              BIGINT             NOT NULL REFERENCES voos(id) ON DELETE RESTRICT,
    classe              classe_enum        NOT NULL,
    tipo_tarifa         tipo_tarifa_enum   NOT NULL,
    preco_base          NUMERIC(10,2)      NOT NULL CHECK (preco_base >= 0),
    franquia_bagagem_kg INTEGER            NOT NULL DEFAULT 23,
    permite_alteracao   BOOLEAN            NOT NULL DEFAULT FALSE,
    permite_reembolso   BOOLEAN            NOT NULL DEFAULT FALSE
);

-- ── Reservas (entidade central) ──
CREATE TABLE reservas (
    id             BIGSERIAL            PRIMARY KEY,
    localizador    VARCHAR(9)           NOT NULL UNIQUE,
    passageiro_id  BIGINT               NOT NULL REFERENCES passageiros(id) ON DELETE RESTRICT,
    voo_id         BIGINT               NOT NULL REFERENCES voos(id)        ON DELETE RESTRICT,
    assento_id     BIGINT               NOT NULL REFERENCES assentos(id)    ON DELETE RESTRICT,
    tarifa_id      BIGINT               NOT NULL REFERENCES tarifas(id)     ON DELETE RESTRICT,
    status_reserva status_reserva_enum  NOT NULL DEFAULT 'PENDING',
    criado_em      TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    atualizado_em  TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

-- ── Pagamentos ──
CREATE TABLE pagamentos (
    id                  BIGSERIAL            PRIMARY KEY,
    reserva_id          BIGINT               NOT NULL REFERENCES reservas(id) ON DELETE RESTRICT,
    metodo_pagamento    metodo_pagamento_enum NOT NULL,
    valor               NUMERIC(10,2)        NOT NULL CHECK (valor >= 0),
    status_pagamento    status_pagamento_enum NOT NULL DEFAULT 'PENDING',
    referencia_gateway  VARCHAR(255),
    processado_em       TIMESTAMPTZ
);

-- ── Bilhetes digitais ──
CREATE TABLE bilhetes (
    id              BIGSERIAL    PRIMARY KEY,
    reserva_id      BIGINT       NOT NULL UNIQUE REFERENCES reservas(id) ON DELETE RESTRICT,
    numero_bilhete  VARCHAR(50)  NOT NULL UNIQUE,
    url_pdf         VARCHAR(500),
    qr_code         VARCHAR(500),
    emitido_em      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Check-ins ──
CREATE TABLE checkins (
    id                   BIGSERIAL    PRIMARY KEY,
    reserva_id           BIGINT       NOT NULL UNIQUE REFERENCES reservas(id) ON DELETE RESTRICT,
    realizado_em         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    documento_verificado VARCHAR(50)  NOT NULL
);

-- ── Cartões de embarque ──
CREATE TABLE cartoes_embarque (
    id               BIGSERIAL    PRIMARY KEY,
    checkin_id       BIGINT       NOT NULL UNIQUE REFERENCES checkins(id) ON DELETE RESTRICT,
    portao           VARCHAR(10),
    limite_embarque  TIMESTAMPTZ,
    url_pdf          VARCHAR(500),
    qr_code          VARCHAR(500),
    emitido_em       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Bagagens extras ──
CREATE TABLE bagagens (
    id                BIGSERIAL          PRIMARY KEY,
    reserva_id        BIGINT             NOT NULL REFERENCES reservas(id) ON DELETE RESTRICT,
    peso_estimado_kg  INTEGER            NOT NULL CHECK (peso_estimado_kg > 0),
    valor_cobrado     NUMERIC(8,2)       NOT NULL DEFAULT 0 CHECK (valor_cobrado >= 0),
    status_bagagem    status_bagagem_enum NOT NULL DEFAULT 'ADDED'
);

-- ── Contas de milhas (1 por passageiro) ──
CREATE TABLE contas_milhas (
    id            BIGSERIAL    PRIMARY KEY,
    passageiro_id BIGINT       NOT NULL UNIQUE REFERENCES passageiros(id) ON DELETE RESTRICT,
    saldo_atual   INTEGER      NOT NULL DEFAULT 0 CHECK (saldo_atual >= 0),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Transações de milhas ──
CREATE TABLE transacoes_milhas (
    id               BIGSERIAL           PRIMARY KEY,
    conta_milhas_id  BIGINT              NOT NULL REFERENCES contas_milhas(id) ON DELETE RESTRICT,
    reserva_id       BIGINT              REFERENCES reservas(id) ON DELETE RESTRICT,
    tipo_transacao   tipo_transacao_enum NOT NULL,
    quantidade       INTEGER             NOT NULL,
    expira_em        TIMESTAMPTZ,
    criado_em        TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

-- ── Notificações operacionais ──
CREATE TABLE notificacoes (
    id                BIGSERIAL            PRIMARY KEY,
    passageiro_id     BIGINT               NOT NULL REFERENCES passageiros(id) ON DELETE RESTRICT,
    voo_id            BIGINT               REFERENCES voos(id) ON DELETE SET NULL,
    tipo_notificacao  tipo_notificacao_enum NOT NULL,
    mensagem          TEXT                 NOT NULL,
    enviada           BOOLEAN              NOT NULL DEFAULT FALSE,
    criado_em         TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

-- ── Índices de desempenho ──
CREATE INDEX idx_reservas_passageiro  ON reservas(passageiro_id);
CREATE INDEX idx_reservas_voo         ON reservas(voo_id);
CREATE INDEX idx_reservas_status      ON reservas(status_reserva);
CREATE INDEX idx_assentos_voo         ON assentos(voo_id);
CREATE INDEX idx_pagamentos_reserva   ON pagamentos(reserva_id);
CREATE INDEX idx_transacoes_conta     ON transacoes_milhas(conta_milhas_id);
CREATE INDEX idx_notificacoes_passag  ON notificacoes(passageiro_id);
CREATE INDEX idx_notificacoes_voo     ON notificacoes(voo_id);

-- ============================================================
--  SEÇÃO 3 — PlantUML  (cole em https://plantuml.com/ie-diagram)
-- ============================================================
/*
@startuml SkyPass_ERD

!define TABLE(name,desc) class name as "desc" << (T,#AAAAFF) >>
!define PK(x) <u>x</u>
!define FK(x) #x
hide empty methods
skinparam classAttributeIconSize 0

TABLE(usuarios, "usuarios") {
  PK(id) : BIGINT
  email : VARCHAR UNIQUE
  senha_hash : VARCHAR
  nome_completo : VARCHAR
  tipo_usuario : ENUM
  ativo : BOOLEAN
}

TABLE(passageiros, "passageiros") {
  PK(id) : BIGINT <<FK usuarios>>
  cpf : VARCHAR(14) UNIQUE
  data_nascimento : DATE
  tipo_documento : ENUM
  numero_documento : VARCHAR
}

TABLE(administradores, "administradores") {
  PK(id) : BIGINT <<FK usuarios>>
  matricula : VARCHAR UNIQUE
  departamento : VARCHAR
}

TABLE(voos, "voos") {
  PK(id) : BIGINT
  numero_voo : VARCHAR(7) UNIQUE
  origem_iata : CHAR(3)
  destino_iata : CHAR(3)
  partida_em : TIMESTAMPTZ
  status_voo : ENUM
}

TABLE(assentos, "assentos") {
  PK(id) : BIGINT
  FK(voo_id) : BIGINT
  codigo : VARCHAR(4)
  classe : ENUM
  status_assento : ENUM
  bloqueado_ate : TIMESTAMPTZ
}

TABLE(tarifas, "tarifas") {
  PK(id) : BIGINT
  FK(voo_id) : BIGINT
  tipo_tarifa : ENUM
  preco_base : NUMERIC
  permite_alteracao : BOOLEAN
  permite_reembolso : BOOLEAN
}

TABLE(reservas, "reservas") {
  PK(id) : BIGINT
  localizador : VARCHAR(9) UNIQUE
  FK(passageiro_id) : BIGINT
  FK(voo_id) : BIGINT
  FK(assento_id) : BIGINT
  FK(tarifa_id) : BIGINT
  status_reserva : ENUM
}

TABLE(pagamentos, "pagamentos") {
  PK(id) : BIGINT
  FK(reserva_id) : BIGINT
  metodo_pagamento : ENUM
  valor : NUMERIC
  status_pagamento : ENUM
  referencia_gateway : VARCHAR
}

TABLE(bilhetes, "bilhetes") {
  PK(id) : BIGINT
  FK(reserva_id) : BIGINT UNIQUE
  numero_bilhete : VARCHAR UNIQUE
  url_pdf : VARCHAR
  qr_code : VARCHAR
}

TABLE(checkins, "checkins") {
  PK(id) : BIGINT
  FK(reserva_id) : BIGINT UNIQUE
  realizado_em : TIMESTAMPTZ
  documento_verificado : VARCHAR
}

TABLE(cartoes_embarque, "cartoes_embarque") {
  PK(id) : BIGINT
  FK(checkin_id) : BIGINT UNIQUE
  portao : VARCHAR
  limite_embarque : TIMESTAMPTZ
  url_pdf : VARCHAR
}

TABLE(bagagens, "bagagens") {
  PK(id) : BIGINT
  FK(reserva_id) : BIGINT
  peso_estimado_kg : INTEGER
  valor_cobrado : NUMERIC
  status_bagagem : ENUM
}

TABLE(contas_milhas, "contas_milhas") {
  PK(id) : BIGINT
  FK(passageiro_id) : BIGINT UNIQUE
  saldo_atual : INTEGER >= 0
  atualizado_em : TIMESTAMPTZ
}

TABLE(transacoes_milhas, "transacoes_milhas") {
  PK(id) : BIGINT
  FK(conta_milhas_id) : BIGINT
  FK(reserva_id) : BIGINT nullable
  tipo_transacao : ENUM
  quantidade : INTEGER
  expira_em : TIMESTAMPTZ
}

TABLE(notificacoes, "notificacoes") {
  PK(id) : BIGINT
  FK(passageiro_id) : BIGINT
  FK(voo_id) : BIGINT nullable
  tipo_notificacao : ENUM
  mensagem : TEXT
  enviada : BOOLEAN
}

usuarios          ||--||  passageiros       : "heranca"
usuarios          ||--||  administradores   : "heranca"
passageiros       ||--o{  reservas          : "faz"
passageiros       ||--||  contas_milhas     : "possui"
passageiros       ||--o{  notificacoes      : "recebe"
voos              ||--|{  assentos          : "tem"
voos              ||--|{  tarifas           : "oferece"
voos              ||--o{  notificacoes      : "gera"
reservas          }o--||  voos              : "para"
reservas          }o--||  assentos          : "ocupa"
reservas          }o--||  tarifas           : "na"
reservas          ||--o{  pagamentos        : "gera"
reservas          ||--o|  bilhetes          : "origina"
reservas          ||--o|  checkins          : "permite"
reservas          ||--o{  bagagens          : "inclui"
checkins          ||--||  cartoes_embarque  : "gera"
contas_milhas     ||--o{  transacoes_milhas : "registra"
transacoes_milhas }o--o|  reservas          : "referencia"

@enduml
*/
